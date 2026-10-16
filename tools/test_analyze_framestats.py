"""Synthetic measurement-format tests, not app performance measurements."""
import argparse
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from analyze_framestats import analyze, budget_ns, MAX_I64


def block(rows, header="Flags,IntendedVsync,FrameCompleted,"):
    return "---PROFILEDATA---\n" + header + "\n" + "\n".join(rows) + "\n---PROFILEDATA---\n"


class FrameStatsTest(unittest.TestCase):
    def test_old_new_reordered_headers_and_integer_precision(self):
        start = 9_007_199_254_740_993  # Above the exact integer range of a double.
        for header, row in [
            ("FLAGS,INTENDED_VSYNC,FRAME_COMPLETED,", f"0,{start},{start+16_000_001},"),
            ("Flags,FrameTimelineVsyncId,IntendedVsync,FrameCompleted,", f"0,9,{start},{start+16_000_001},"),
            ("FrameCompleted,FutureColumn,Flags,IntendedVsync", f"{start+16_000_001},ignored,0,{start}"),
        ]:
            result = analyze(block([row], header), 16_000_000)["blocks"][0]
            self.assertEqual(1, result["valid_frames"])
            self.assertEqual(16.000001, result["duration_ms"]["p95"])
            self.assertEqual(1, result["over_explicit_budget"]["frames"])

    def test_known_percentiles_and_exact_budget_boundary(self):
        rows = [f"0,{i*200_000_000},{i*200_000_000+i*1_000_000}," for i in range(1, 101)]
        result = analyze(block(rows), 95_000_000)["blocks"][0]
        self.assertEqual({"p50": 50, "p95": 95, "p99": 99, "max": 100}, result["duration_ms"])
        self.assertEqual(5, result["over_explicit_budget"]["frames"])
        self.assertEqual(.05, result["over_explicit_budget"]["fraction"])

    def test_exclusions_are_visible_not_zero_duration_samples(self):
        rows = ["0,100,200,", "0,100,200,", "1,0,9223372036854775807,", "0,0,200,",
                "0,200,100,", "0,100,100,", f"0,100,{MAX_I64},", "0,100,NaN,", "0,100,200,extra,"]
        result = analyze(block(rows))["blocks"][0]
        self.assertEqual(9, result["rows"])
        self.assertEqual(1, result["valid_frames"])
        self.assertEqual({"malformed": 1, "nonzero_flags": 1, "invalid_timing": 5, "duplicate": 1}, result["excluded"])
        self.assertIsNone(result["over_explicit_budget"])

    def test_empty_valid_set_is_unknown_not_success(self):
        result = analyze(block(["1,0,0,"]), 16_000_000)
        self.assertEqual("no_valid_samples", result["status"])
        self.assertIsNone(result["blocks"][0]["duration_ms"]["p95"])
        self.assertIsNone(result["blocks"][0]["over_explicit_budget"]["fraction"])

    def test_windows_are_separate_and_identifiers_not_exported(self):
        result = analyze("Window secret-provider-title\n" + block(["0,100,200,"]) +
                         "Window other-secret\n" + block(["0,100,100000100,"]))
        self.assertEqual(2, len(result["blocks"]))
        self.assertEqual(.0001, result["blocks"][0]["duration_ms"]["max"])
        self.assertEqual(100, result["blocks"][1]["duration_ms"]["max"])
        self.assertNotIn("secret", json.dumps(result))

    def test_truncated_missing_ambiguous_and_unknown_schemas_fail(self):
        for data in ["No process found", "---PROFILEDATA---\nFlags,IntendedVsync,FrameCompleted\n0,1,2",
                     block([], "Flags,Other"), block([], "Flags,IntendedVsync,FrameCompleted,Flags"),
                     block([], "Flags,IntendedVsync,FrameCompleted,,")]:
            with self.subTest(data=data), self.assertRaises(ValueError):
                analyze(data)

    def test_very_slow_positive_frame_is_not_silently_discarded(self):
        result = analyze(block(["0,100,200000000100,"]))["blocks"][0]
        self.assertEqual(200000, result["duration_ms"]["max"])

    def test_budget_validation_and_no_implicit_refresh_assumption(self):
        self.assertEqual(16_666_667, budget_ns("16.666667"))
        for value in ["NaN", "Infinity", "-1", "0", "0.0000001", "oops", "1e99"]:
            with self.subTest(value=value), self.assertRaises(argparse.ArgumentTypeError):
                budget_ns(value)
        for value in [True, 0, -1, 1.5, MAX_I64]:
            with self.subTest(value=value), self.assertRaises(ValueError):
                analyze(block(["0,1,2,"]), value)

    def test_cli_safe_output_and_no_overwrite(self):
        with tempfile.TemporaryDirectory() as temporary:
            source = Path(temporary)/"input.txt"
            destination = Path(temporary)/"report.json"
            source.write_text(block(["0,100,200,"]), encoding="utf-8")
            command = [sys.executable, str(Path(__file__).with_name("analyze_framestats.py")), str(source),
                       "--output", str(destination)]
            first = subprocess.run(command, capture_output=True)
            self.assertEqual(0, first.returncode, first.stderr)
            saved = destination.read_bytes()
            self.assertEqual(64, len(json.loads(saved)["input_sha256"]))
            second = subprocess.run(command, capture_output=True)
            self.assertEqual(2, second.returncode)
            self.assertEqual(saved, destination.read_bytes())
            source.write_text(block(["1,0,0,"]), encoding="utf-8")
            empty = subprocess.run(command[:-2], capture_output=True)
            self.assertEqual(2, empty.returncode)
            self.assertEqual("no_valid_samples", json.loads(empty.stdout)["status"])


if __name__ == "__main__":
    unittest.main()
