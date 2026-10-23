package dev.iptvplayer.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import dev.iptvplayer.tv.R
import dev.iptvplayer.tv.SourceDraft
import dev.iptvplayer.tv.TvState
import dev.iptvplayer.tv.TvViewModel
import dev.iptvplayer.tv.CatalogOperation
import dev.iptvplayer.tv.data.PhoneSetupServer
import dev.iptvplayer.tv.data.PhoneSetupSession
import dev.iptvplayer.tv.data.PhoneSubmission
import dev.iptvplayer.tv.data.SourceType
import dev.iptvplayer.tv.data.isUnencrypted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Adding a source (SETUP_FLOW, D117): a welcome with two ways in, the phone way with a QR code and the remote way with
 * one question per screen. Editing a saved source keeps the single form ([Setup] in PilotApp).
 */
internal enum class SetupStep { WELCOME, PHONE, CONFIRM, TYPE, ADDRESS, USERNAME, PASSWORD, NAME, ERROR }

/** Where the phone page is served: the TV's address on the home network and the port the server took. */
private data class PhoneLink(val host: String, val port: Int) { val address get() = "http://$host:$port" }

@Composable internal fun SetupFlow(s: TvState, model: TvViewModel) {
    val defaultName = stringResource(R.string.source_default_name)
    val unnamed = stringResource(R.string.source_unnamed)
    val required = stringResource(R.string.setup_required)
    val invalidAddress = stringResource(R.string.failure_address_invalid)
    // The flow leaves the screen while a source connects; it comes back on the step that fits how that ended.
    var step by rememberSaveable { mutableStateOf(when {
        s.error != null && s.draft.address.isNotBlank() -> SetupStep.ERROR
        s.draft.address.isNotBlank() -> SetupStep.NAME
        else -> SetupStep.WELCOME
    }) }
    var fieldError by remember { mutableStateOf<String?>(null) }
    val xtream = s.draft.type == SourceType.XTREAM

    // The phone way: a small server on the home network while its screens are open and the app is on screen.
    val context = LocalContext.current
    val received = remember { MutableStateFlow<PhoneSubmission?>(null) }
    val server = remember {
        PhoneSetupServer(page = { context.assets.open("setup/index.html").use { it.readBytes() } }) { received.value = it }
    }
    var session by remember { mutableStateOf(PhoneSetupSession(PhoneSetupSession.newCode(), System.currentTimeMillis())) }
    SideEffect { server.session = session }
    fun newCode() {
        received.value = null
        session = PhoneSetupSession(PhoneSetupSession.newCode(), System.currentTimeMillis())
        server.session = session
    }
    var link by remember { mutableStateOf<PhoneLink?>(null) }
    var offline by remember { mutableStateOf(false) }
    val started = LocalLifecycleOwner.current.lifecycle.currentStateAsState().value.isAtLeast(Lifecycle.State.STARTED)
    val serving = started && (step == SetupStep.PHONE || step == SetupStep.CONFIRM)
    LaunchedEffect(serving) {
        if (!serving) return@LaunchedEffect
        try {
            val host = withContext(Dispatchers.IO) { PhoneSetupServer.localAddress() }
            offline = host == null
            if (host != null) link = PhoneLink(host, server.start(this))
            awaitCancellation()
        } finally { server.stop(); link = null }
    }
    val submission by received.collectAsState()
    LaunchedEffect(submission) { if (submission != null && step == SetupStep.PHONE) step = SetupStep.CONFIRM }

    fun go(next: SetupStep) {
        fieldError = null
        if (model.state.value.error != null) model.draft(model.state.value.draft)
        if (next == SetupStep.PHONE && step != SetupStep.CONFIRM) newCode()
        step = next
    }
    fun back() = when (step) {
        SetupStep.WELCOME -> Unit
        SetupStep.PHONE, SetupStep.TYPE -> go(SetupStep.WELCOME)
        SetupStep.CONFIRM -> { newCode(); go(SetupStep.PHONE) }
        SetupStep.ADDRESS -> go(SetupStep.TYPE)
        SetupStep.USERNAME -> go(SetupStep.ADDRESS)
        SetupStep.PASSWORD -> go(SetupStep.USERNAME)
        SetupStep.NAME -> go(if (xtream) SetupStep.PASSWORD else SetupStep.ADDRESS)
        SetupStep.ERROR -> go(SetupStep.ADDRESS)
    }
    BackHandler(step != SetupStep.WELCOME) { back() }
    // A required field left empty keeps focus where it is and says so under it (SETUP_FLOW "Boş").
    fun advance() {
        val d = model.state.value.draft
        when (step) {
            SetupStep.ADDRESS -> when {
                d.address.isBlank() -> fieldError = required
                d.address.trim().toHttpUrlOrNull() == null -> fieldError = invalidAddress
                else -> go(if (d.type == SourceType.XTREAM) SetupStep.USERNAME else SetupStep.NAME)
            }
            SetupStep.USERNAME -> if (d.username.isBlank()) fieldError = required else go(SetupStep.PASSWORD)
            SetupStep.PASSWORD -> if (d.password.isEmpty()) fieldError = required else go(SetupStep.NAME)
            SetupStep.NAME -> model.connectDraft(defaultName, unnamed)
            else -> Unit
        }
    }
    val total = if (xtream) 5 else 3

    Box(Modifier.fillMaxSize().background(Ink.Background).testTag("setup-flow")) {
        key(step) {
            when (step) {
                SetupStep.WELCOME -> Welcome(s, phone = { go(SetupStep.PHONE) }, remote = { go(SetupStep.TYPE) },
                    cancel = if (s.source != null) ({ model.back() }) else null)
                SetupStep.PHONE -> PhoneStep(link, offline, session, remote = { go(SetupStep.TYPE) }, newCode = { newCode() },
                    cancel = { go(SetupStep.WELCOME) })
                SetupStep.CONFIRM -> submission?.let { sent ->
                    ConfirmStep(sent, session.code, defaultName, s.error?.resolve(), add = {
                        model.draft(SourceDraft(name = sent.name.ifBlank { null }, type = sent.type, address = sent.address,
                            username = sent.username, password = sent.password, epgUrl = sent.epgUrl))
                        model.connectDraft(defaultName, unnamed)
                    }, fix = {
                        model.draft(SourceDraft(name = sent.name.ifBlank { null }, type = sent.type, address = sent.address,
                            username = sent.username, password = sent.password, epgUrl = sent.epgUrl))
                        go(SetupStep.ADDRESS)
                    }, cancel = { back() })
                } ?: LaunchedEffect(Unit) { step = SetupStep.PHONE }
                SetupStep.TYPE -> TypeStep(s.draft) { type ->
                    if (type != s.draft.type) model.draft(s.draft.copy(type = type))
                    go(SetupStep.ADDRESS)
                }
                SetupStep.ADDRESS -> FieldStep(2, total,
                    stringResource(if (xtream) R.string.setup_server_title else R.string.setup_m3u),
                    stringResource(if (xtream) R.string.setup_server_hint else R.string.setup_address_hint),
                    stringResource(if (xtream) R.string.setup_server else R.string.setup_m3u), s.draft.address, fieldError,
                    onChange = { model.draft(model.state.value.draft.copy(address = it)); fieldError = null },
                    next = ::advance, back = ::back)
                SetupStep.USERNAME -> FieldStep(3, total, stringResource(R.string.setup_username),
                    stringResource(R.string.setup_account_hint), stringResource(R.string.setup_username), s.draft.username, fieldError,
                    onChange = { model.draft(model.state.value.draft.copy(username = it)); fieldError = null },
                    next = ::advance, back = ::back)
                SetupStep.PASSWORD -> FieldStep(4, total, stringResource(R.string.setup_password),
                    stringResource(R.string.setup_account_hint), stringResource(R.string.setup_password), s.draft.password, fieldError,
                    onChange = { model.draft(model.state.value.draft.copy(password = it)); fieldError = null },
                    next = ::advance, back = ::back, secret = true)
                SetupStep.NAME -> NameStep(s, total, defaultName, connect = ::advance, back = ::back,
                    rename = { model.draft(model.state.value.draft.copy(name = it)) },
                    guide = { model.draft(model.state.value.draft.copy(epgUrl = it)) })
                SetupStep.ERROR -> ErrorStep(s.error?.resolve().orEmpty(), retry = { model.connectDraft(defaultName, unnamed) },
                    fix = { go(SetupStep.ADDRESS) }, cancel = {
                        if (s.source != null) model.back() else { model.draft(SourceDraft()); go(SetupStep.WELCOME) }
                    })
            }
        }
    }
}

/** Focus for a step's first control once it is laid out; each step composes anew under key(step). */
@Composable private fun rememberFirstFocus(): FocusRequester {
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) { withFrameNanos { }; runCatching { requester.requestFocus() } }
    return requester
}

/** TV-001: the brand on the left, the two ways in on the right; the phone way comes first. */
@Composable private fun Welcome(s: TvState, phone: () -> Unit, remote: () -> Unit, cancel: (() -> Unit)?) {
    val first = rememberFirstFocus()
    Row(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 40.dp), horizontalArrangement = Arrangement.spacedBy(48.dp)) {
        Column(Modifier.weight(.9f).align(Alignment.CenterVertically), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            BrandLogo(height = 28.dp)
            Text(stringResource(R.string.setup_title), color = Ink.Text, fontFamily = Roboto, fontSize = 36.sp, lineHeight = 42.sp)
            Note(stringResource(R.string.setup_tagline))
            val startOver = s.catalogNotice?.takeIf { it.operation == CatalogOperation.START_OVER }
            if (startOver == null) Note(stringResource(R.string.setup_privacy))
            startOver?.let { Note(it.message.resolve(), error = it.needsAttention, modifier = Modifier.testTag("setup-notice")) }
        }
        Column(Modifier.weight(1.1f).align(Alignment.CenterVertically), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ChoiceCard("phone", stringResource(R.string.setup_way_phone), stringResource(R.string.setup_way_phone_hint),
                Modifier.focusRequester(first).testTag("setup-phone"), badge = stringResource(R.string.setup_recommended), onClick = phone)
            ChoiceCard("remote", stringResource(R.string.setup_way_remote), stringResource(R.string.setup_way_remote_hint),
                Modifier.testTag("setup-remote"), onClick = remote)
            if (cancel != null) Action(stringResource(R.string.action_cancel), Modifier.testTag("setup-cancel"), outlined = true, onClick = cancel)
        }
    }
}

/** A large choice: an icon, a title and a line under it. A surface like [RadioRow]: focus fills it inside the ring. */
@Composable private fun ChoiceCard(icon: String, title: String, description: String, modifier: Modifier = Modifier,
    badge: String? = null, onClick: () -> Unit) {
    val radius = 18.dp
    Button(onClick = onClick, modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(radius)),
        colors = ButtonDefaults.colors(containerColor = Ink.Container, contentColor = Ink.Text,
            focusedContainerColor = Ink.High, focusedContentColor = Ink.Text),
        scale = ButtonDefaults.scale(focusedScale = 1f), border = ButtonDefaults.border(focusedBorder = focusRing(radius))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(52.dp).background(Ink.Selected, CircleShape), contentAlignment = Alignment.Center) {
                LineIcon(icon, Ink.Mint, Modifier.size(26.dp).clearAndSetSemantics { })
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(title, color = Ink.Text, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp)
                    if (badge != null) Text(badge, Modifier.background(Ink.Mint, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Ink.OnMint, fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp)
                }
                Note(description)
            }
        }
    }
}

/** The frame of the remote way's screens: the logo and "Adım 2 / 5" on top, one question under them near the top. */
@Composable private fun StepFrame(position: Int?, total: Int, title: String, hint: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(height = 22.dp)
            Spacer(Modifier.weight(1f))
            if (position != null) Note(stringResource(R.string.setup_step, position, total))
        }
        Heading(title)
        if (hint != null) Note(hint, Modifier.widthIn(max = 720.dp))
        Spacer(Modifier.height(8.dp))
        content()
    }
}

/**
 * TV-002: M3U link or Xtream account; the kind already in the draft takes focus once an address was typed. The step
 * count follows the focused card: the link takes three steps, the account five.
 */
@Composable private fun TypeStep(draft: SourceDraft, choose: (SourceType) -> Unit) {
    val first = rememberFirstFocus()
    val xtreamFirst = draft.type == SourceType.XTREAM && draft.address.isNotBlank()
    var xtreamFocused by remember { mutableStateOf(xtreamFirst) }
    StepFrame(1, if (xtreamFocused) 5 else 3, stringResource(R.string.setup_type_title), null) {
        Row(Modifier.widthIn(max = 860.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChoiceCard("guide", stringResource(R.string.setup_m3u), stringResource(R.string.setup_type_m3u_hint),
                Modifier.weight(1f).then(if (!xtreamFirst) Modifier.focusRequester(first) else Modifier)
                    .onFocusChanged { if (it.isFocused) xtreamFocused = false }.testTag("setup-m3u")) { choose(SourceType.M3U) }
            ChoiceCard("tv", stringResource(R.string.setup_type_xtream), stringResource(R.string.setup_type_xtream_hint),
                Modifier.weight(1f).then(if (xtreamFirst) Modifier.focusRequester(first) else Modifier)
                    .onFocusChanged { if (it.isFocused) xtreamFocused = true }.testTag("setup-xtream")) { choose(SourceType.XTREAM) }
        }
    }
}

/**
 * TV-002a, TV-003a–c: one field near the top, so the TV keyboard never covers it. OK opens the keyboard (D058); its
 * Next moves on. The password is readable (D116) and can be hidden while someone else is in the room.
 */
@Composable private fun FieldStep(position: Int, total: Int, title: String, hint: String, label: String, value: String,
    error: String?, onChange: (String) -> Unit, next: () -> Unit, back: () -> Unit, secret: Boolean = false) {
    val first = rememberFirstFocus()
    var reveal by rememberSaveable { mutableStateOf(true) }
    StepFrame(position, total, title, hint) {
        EntryField(label, value, onChange, Modifier.widthIn(max = 760.dp).fillMaxWidth().focusRequester(first).testTag("setup-field"),
            secret = secret, onDone = next, revealed = reveal, doneAction = ImeAction.Next)
        Box(Modifier.heightIn(min = 19.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            if (error != null) Note(error, error = true, modifier = Modifier.testTag("setup-error"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Action(stringResource(R.string.setup_next), Modifier.testTag("setup-next"), primary = true, onClick = next)
            Action(stringResource(R.string.action_back), outlined = true, onClick = back)
            if (secret) Action(stringResource(if (reveal) R.string.setup_password_hide else R.string.setup_password_show),
                Modifier.testTag("setup-reveal"), outlined = true, selected = reveal) { reveal = !reveal }
        }
    }
}

/** TV-002b: the name, filled with the default, and the optional guide; Bağlan has focus. */
@Composable private fun NameStep(s: TvState, total: Int, defaultName: String, connect: () -> Unit, back: () -> Unit,
    rename: (String) -> Unit, guide: (String) -> Unit) {
    val first = rememberFirstFocus()
    StepFrame(total, total, stringResource(R.string.setup_name_title), stringResource(R.string.setup_name_hint)) {
        Row(Modifier.widthIn(max = 860.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EntryField(stringResource(R.string.setup_name), s.draft.name ?: defaultName, rename, Modifier.weight(1f))
            EntryField(stringResource(R.string.setup_guide), s.draft.epgUrl, guide, Modifier.weight(1.4f))
        }
        if (isUnencrypted(s.draft.address)) HttpNote()
        Box(Modifier.heightIn(min = 19.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            s.error?.let { Note(it.resolve(), error = true, modifier = Modifier.testTag("setup-error")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Action(stringResource(R.string.setup_connect_now), Modifier.focusRequester(first).testTag("setup-connect"), primary = true, onClick = connect)
            Action(stringResource(R.string.action_back), outlined = true, onClick = back)
        }
    }
}

/** TV-004b: one plain sentence and the three ways on. Back is Bilgileri düzelt. */
@Composable private fun ErrorStep(message: String, retry: () -> Unit, fix: () -> Unit, cancel: () -> Unit) {
    val first = rememberFirstFocus()
    StepFrame(null, 0, stringResource(R.string.setup_failed_title), null) {
        Note(message, Modifier.widthIn(max = 720.dp).semantics { liveRegion = LiveRegionMode.Polite }.testTag("setup-error"), error = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Action(stringResource(R.string.action_retry), Modifier.focusRequester(first).testTag("setup-retry"), primary = true, onClick = retry)
            Action(stringResource(R.string.setup_fix), Modifier.testTag("setup-fix"), outlined = true, onClick = fix)
            Action(stringResource(R.string.action_cancel), outlined = true, onClick = cancel)
        }
    }
}

/**
 * TV-001a: what to do on the left, the QR code, the address and the code on the right. The code lasts five minutes;
 * Yeni kod starts another, also after the phone was locked out by wrong codes.
 */
@Composable private fun PhoneStep(link: PhoneLink?, offline: Boolean, session: PhoneSetupSession, remote: () -> Unit,
    newCode: () -> Unit, cancel: () -> Unit) {
    val first = rememberFirstFocus()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(session) { while (true) { now = System.currentTimeMillis(); delay(1_000) } }
    val remaining = session.remainingMs(now)
    val expired = remaining == 0L
    Row(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 32.dp), horizontalArrangement = Arrangement.spacedBy(48.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            BrandLogo(height = 22.dp)
            Spacer(Modifier.height(20.dp))
            Heading(stringResource(R.string.setup_way_phone))
            listOf(R.string.setup_phone_step1, R.string.setup_phone_step2, R.string.setup_phone_step3).forEachIndexed { index, text ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(30.dp).background(Ink.Selected, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", Modifier.clearAndSetSemantics { }, color = Ink.Mint, fontFamily = Roboto,
                            fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                    Text(stringResource(text), color = Ink.Text, fontFamily = Roboto, fontSize = 17.sp, lineHeight = 23.sp)
                }
            }
            Note(stringResource(R.string.setup_phone_network))
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Action(stringResource(R.string.action_cancel), Modifier.focusRequester(first).testTag("setup-phone-cancel"), onClick = cancel)
                Action(stringResource(R.string.setup_way_remote), Modifier.testTag("setup-phone-remote"), outlined = true, onClick = remote)
                Action(stringResource(R.string.setup_phone_new_code), Modifier.testTag("setup-new-code"), outlined = true, onClick = newCode)
            }
        }
        Column(Modifier.width(320.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
            when {
                offline -> Note(stringResource(R.string.setup_phone_offline), Modifier.testTag("setup-phone-offline"), error = true)
                link == null -> { LoadingArc(); Note(stringResource(R.string.setup_phone_starting)) }
                else -> {
                    QrCode("${link.address}/#${session.code}", 248.dp, stringResource(R.string.setup_phone_qr),
                        Modifier.alpha(if (expired) .2f else 1f).testTag("setup-qr"))
                    Note(stringResource(R.string.setup_phone_address))
                    Text(link.address, color = Ink.Text, fontFamily = Roboto, fontSize = 17.sp, lineHeight = 22.sp)
                    CodeText(session.code, if (expired) Ink.Muted else Ink.Mint)
                    Box(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                        if (expired) Note(stringResource(R.string.setup_phone_expired), error = true)
                        else Note(stringResource(R.string.setup_phone_valid, clockTime(remaining + 999)))
                    }
                }
            }
        }
    }
}

/** The six digits in two groups of three; TalkBack reads them one by one. */
@Composable private fun CodeText(code: String, color: androidx.compose.ui.graphics.Color, size: Dp = 40.dp) {
    val spoken = code.toList().joinToString(" ")
    Text("${code.take(3)} ${code.drop(3)}", Modifier.semantics { contentDescription = spoken }, color = color, fontFamily = Roboto,
        fontWeight = FontWeight.Medium, fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { size.toSp() },
        letterSpacing = 4.sp)
}

/**
 * TV-001b: what the phone sent, without the password or the link itself; the same code as on the phone. Only Ekle
 * connects, and only the TV talks to the provider.
 */
@Composable private fun ConfirmStep(sent: PhoneSubmission, code: String, defaultName: String, error: String?, add: () -> Unit,
    fix: () -> Unit, cancel: () -> Unit) {
    val first = rememberFirstFocus()
    StepFrame(null, 0, stringResource(R.string.setup_confirm_title), stringResource(R.string.setup_confirm_secret)) {
        Column(Modifier.widthIn(min = 420.dp, max = 640.dp).background(Ink.Container, RoundedCornerShape(18.dp)).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ConfirmRow(stringResource(R.string.setup_confirm_name), sent.name.ifBlank { defaultName })
            ConfirmRow(stringResource(R.string.setup_confirm_type),
                stringResource(if (sent.type == SourceType.XTREAM) R.string.setup_type_xtream else R.string.setup_m3u))
            sent.address.trim().toHttpUrlOrNull()?.host?.let { ConfirmRow(stringResource(R.string.setup_confirm_server), it) }
            ConfirmRow(stringResource(R.string.setup_confirm_guide),
                stringResource(if (sent.epgUrl.isNotBlank()) R.string.setup_confirm_guide_yes else R.string.setup_confirm_guide_no))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.setup_phone_code), Modifier.width(120.dp), color = Ink.Muted, fontFamily = Roboto, fontSize = 15.sp)
                CodeText(code, Ink.Mint, 24.dp)
            }
        }
        if (isUnencrypted(sent.address)) HttpNote()
        Box(Modifier.heightIn(min = 19.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            if (error != null) Note(error, error = true, modifier = Modifier.testTag("setup-error"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Action(stringResource(R.string.setup_confirm_add), Modifier.focusRequester(first).testTag("setup-confirm"), primary = true, onClick = add)
            if (error != null) Action(stringResource(R.string.setup_fix), outlined = true, onClick = fix)
            Action(stringResource(R.string.action_cancel), outlined = true, onClick = cancel)
        }
    }
}

/** What an http:// address means for the account in it (isUnencrypted), before the source is added; it does not stop it. */
@Composable internal fun HttpNote(modifier: Modifier = Modifier) {
    Note(stringResource(R.string.setup_http_warning), modifier.widthIn(max = 720.dp).testTag("setup-http"))
}

@Composable private fun ConfirmRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(120.dp), color = Ink.Muted, fontFamily = Roboto, fontSize = 15.sp, lineHeight = 21.sp)
        Text(value, color = Ink.Text, fontFamily = Roboto, fontSize = 17.sp, lineHeight = 23.sp, maxLines = 1)
    }
}
