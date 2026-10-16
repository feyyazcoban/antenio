"""Offline gfxinfo framestats diagnostics. Does not connect to or mutate a device."""
from __future__ import annotations

import argparse
from collections import Counter
from decimal import Decimal, InvalidOperation
import hashlib
import json
from math import ceil
from pathlib import Path

MARKER = "---PROFILEDATA---"
MAX_I64 = (1 << 63) - 1
MAX_BYTES = 64 * 1024 * 1024
REQUIRED = {"flags", "intendedvsync", "framecompleted"}


def budget_ns(value: str) -> int:
    try:
        ns = Decimal(value) * 1_000_000
        if not ns.is_finite() or ns <= 0 or ns >= MAX_I64 or ns != ns.to_integral_value():
            raise ValueError
        return int(ns)
    except (InvalidOperation, ValueError):
        raise argparse.ArgumentTypeError("Budget must be positive, finite and representable in whole nanoseconds") from None


def cells(line: str) -> list[str]:
    values = [part.strip() for part in line.split(",")]
    if values and values[-1] == "":
        values.pop()  # Android emits a trailing comma on some versions.
    return values


def integer(value: str) -> int:
    # Bound conversion and reject floating point / unknown timestamps.
    if not value or len(value) > 20 or not value.isascii() or not value.isdecimal():
        raise ValueError
    number = int(value)
    if number >= MAX_I64:
        raise ValueError
    return number


def summarize(lines: list[str], index: int, frame_budget_ns: int | None) -> dict:
    if not lines:
        raise ValueError(f"Profile block {index} has no header")
    header = [part.replace("_", "").casefold() for part in cells(lines[0])]
    if not REQUIRED <= set(header) or len(set(header)) != len(header) or "" in header:
        raise ValueError(f"Profile block {index} has an unsupported or ambiguous header")
    positions = {name: header.index(name) for name in REQUIRED}
    reasons = Counter()
    durations = []
    seen = set()
    for line in lines[1:]:
        values = cells(line)
        if len(values) != len(header):
            reasons["malformed"] += 1
            continue
        try:
            flags = integer(values[positions["flags"]])
            if flags:
                reasons["nonzero_flags"] += 1
                continue
            start = integer(values[positions["intendedvsync"]])
            end = integer(values[positions["framecompleted"]])
            if start == 0 or end <= start:
                raise ValueError
        except ValueError:
            reasons["invalid_timing"] += 1
            continue
        key = tuple(values)
        if key in seen:
            reasons["duplicate"] += 1
            continue
        seen.add(key)
        durations.append(end - start)
    durations.sort()
    count = len(durations)
    def percentile(fraction: float):
        return durations[ceil(fraction * count) - 1] / 1_000_000 if count else None
    return {
        "block": index,
        "rows": len(lines) - 1,
        "valid_frames": count,
        "excluded": {name: reasons[name] for name in ("malformed", "nonzero_flags", "invalid_timing", "duplicate")},
        "duration_ms": {"p50": percentile(.50), "p95": percentile(.95), "p99": percentile(.99),
                        "max": durations[-1] / 1_000_000 if count else None},
        "over_explicit_budget": None if frame_budget_ns is None else {
            "budget_ms": frame_budget_ns / 1_000_000,
            "frames": sum(value > frame_budget_ns for value in durations),
            "fraction": sum(value > frame_budget_ns for value in durations) / count if count else None,
        },
    }


def analyze(text: str, frame_budget_ns: int | None = None) -> dict:
    if frame_budget_ns is not None and (isinstance(frame_budget_ns, bool) or not isinstance(frame_budget_ns, int)
                                       or not 0 < frame_budget_ns < MAX_I64):
        raise ValueError("Invalid nanosecond budget")
    blocks = []
    current = None
    for line in text.splitlines():
        if line.strip() == MARKER:
            if current is None:
                current = []
            else:
                blocks.append(summarize(current, len(blocks) + 1, frame_budget_ns))
                current = None
        elif current is not None and line.strip():
            current.append(line.strip())
    if current is not None:
        raise ValueError("Unclosed profile block; capture may be truncated")
    if not blocks:
        raise ValueError("No profile blocks; absence of data is not a performance pass")
    return {
        "schema_version": 1,
        "metric": "FrameCompleted minus IntendedVsync; Flags=0 only",
        "percentile_method": "nearest-rank",
        "status": "samples_available" if any(b["valid_frames"] for b in blocks) else "no_valid_samples",
        "blocks": blocks,
        "limitations": [
            "Recent frame buffer only; does not prove complete scenario coverage.",
            "Blocks are separate streams and are not pooled; window identities are omitted.",
            "No FPS, input-to-pixel latency, displayed-frame jank or product pass/fail is inferred.",
            "Nonzero flags are excluded and counted; first-frame/startup work needs separate measurement.",
            "Explicit budget exceedance is diagnostic, not Android FrameTimeline jank classification.",
        ],
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path)
    parser.add_argument("--frame-budget-ms", type=budget_ns, default=None)
    parser.add_argument("--output", type=Path, help="Create a new report; never overwrite an existing file")
    args = parser.parse_args()
    try:
        with args.input.open("rb") as stream:
            raw = stream.read(MAX_BYTES + 1)
        if len(raw) > MAX_BYTES:
            raise ValueError("Input exceeds 64 MiB limit")
        report = analyze(raw.decode("utf-8-sig"), args.frame_budget_ms)
        report["input_sha256"] = hashlib.sha256(raw).hexdigest()
        output = json.dumps(report, ensure_ascii=False, indent=2, allow_nan=False) + "\n"
        if args.output:
            with args.output.open("x", encoding="utf-8") as stream:
                stream.write(output)
        else:
            print(output, end="")
        return 0 if report["status"] == "samples_available" else 2
    except (OSError, UnicodeError):
        parser.exit(2, "Cannot read input or create report; no existing file was overwritten.\n")
    except ValueError as error:
        parser.exit(2, f"{error}\n")


if __name__ == "__main__":
    raise SystemExit(main())
