from pathlib import Path
import json,subprocess,time
import argparse
parser=argparse.ArgumentParser(description="Destructive synthetic cold-restore check on the dedicated design emulator only.")
parser.add_argument("--adb",required=True,help="Path to Android SDK adb executable")
parser.add_argument("--serial",required=True,help="Explicit emulator serial; physical devices are refused")
parser.add_argument("--allow-fixture-reset",action="store_true",help="Confirm that dedicated emulator contains disposable test data")
parser.add_argument("--output",default=".private/cold-catalog-audit")
args=parser.parse_args()
if not args.allow_fixture_reset or not args.serial.startswith("emulator-"):
 parser.error("Requires --allow-fixture-reset and an emulator serial")
r=Path(__file__).resolve().parents[1]
audit_dir=Path(args.output).resolve();audit_dir.mkdir(parents=True,exist_ok=True)
a=args.adb
def adb(*command):return subprocess.check_output([a,'-s',args.serial,*command],timeout=180)
if adb('emu','avd','name').decode().splitlines()[0].strip() != 'nextv_design_api36':
 raise SystemExit('Refusing a device other than the dedicated nextv_design_api36 AVD')
adb('install','-r',str(r/'apps/android-tv/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'))
def test(method,flag):
 out=adb('shell','am','instrument','-w','-e',flag,'true','-e','class',f'dev.iptvplayer.tv.CatalogColdLaunchTest#{method}','app.antenio.tv.test/androidx.test.runner.AndroidJUnitRunner')
 (audit_dir/f'{flag}.log').write_bytes(out);print(out.decode(errors='replace')[-2500:],flush=True);assert b'OK (1 test)' in out
try:
 test('prepareEncryptedCatalogForColdLaunch','prepareColdCatalog')
 adb('shell','am','force-stop','app.antenio.tv')
 launch=adb('shell','am','start','-W','-n','app.antenio.tv/dev.iptvplayer.tv.MainActivity')
 (audit_dir/'cold-external-launch.log').write_bytes(launch);print(launch.decode(),flush=True)
 pid=adb('shell','pidof','app.antenio.tv').decode().strip();assert pid
 time.sleep(8)
 adb('shell','uiautomator','dump','/sdcard/cold-check.xml')
 xml=adb('exec-out','cat','/sdcard/cold-check.xml')
 (audit_dir/'cold-external-ui.xml').write_bytes(xml)
 assert 'İzlemeye devam et'.encode() in xml
 (audit_dir/'home.png').write_bytes(adb('exec-out','screencap','-p'))
 test('verifyRestoredAfterProcessRestart','verifyColdCatalog')
 (audit_dir/'cold-restore-verification.json').write_bytes(adb('exec-out','run-as','app.antenio.tv','cat','files/cold-restore-verification.json'))
 print((audit_dir/'cold-restore-verification.json').read_text(),flush=True)
finally:
 test('cleanColdCatalogFixture','cleanColdCatalog')
 adb('shell','am','force-stop','app.antenio.tv')
