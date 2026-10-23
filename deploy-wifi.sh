#!/bin/zsh
# Instala o app do time no Control Hub pelo Wi-Fi, sem precisar de cabo.
#
# O que ele faz, nesta ordem:
#   1. compila o APK (ainda com internet, porque o Gradle pode precisar baixar algo)
#   2. troca o Wi-Fi do Mac para a rede do Control Hub
#   3. instala o APK e reinicia o app do Robot Controller
#   4. volta o Wi-Fi do Mac para a rede que estava antes
#
# Uso:  ./deploy-wifi.sh
# Durante o passo 2 e 3 o Mac fica sem internet por cerca de 1 minuto.

set -u
REDE_HUB="FTC-kWD4"        # nome da rede Wi-Fi do Control Hub
SENHA_HUB="password"       # senha da rede do hub
IP_HUB="192.168.43.1"
WIFI_IF="en0"
ADB="$HOME/Library/Android/sdk/platform-tools/adb"
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
APK="TeamCode/build/outputs/apk/debug/TeamCode-debug.apk"
ACTIVITY="com.qualcomm.ftcrobotcontroller/org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity"

cd "$(dirname "$0")"

# Nome da rede Wi-Fi atual. (networksetup -getairportnetwork nao funciona neste macOS.)
rede_atual() {
  ipconfig getsummary "$WIFI_IF" 2>/dev/null | awk '/^ *SSID : /{sub(/^ *SSID : /, ""); print; exit}'
}

REDE_ORIGINAL=$(rede_atual)
echo ">> Rede atual: ${REDE_ORIGINAL:-(nao identificada)}"

tem_internet() { ping -c 1 -W 2 8.8.8.8 >/dev/null 2>&1; }

volta_rede() {
  if [ -n "$REDE_ORIGINAL" ] && [ "$REDE_ORIGINAL" != "$REDE_HUB" ]; then
    echo ">> Voltando para a rede: $REDE_ORIGINAL"
    networksetup -setairportnetwork "$WIFI_IF" "$REDE_ORIGINAL" >/dev/null 2>&1
    sleep 6
  fi
  if ! tem_internet; then
    # Desliga e liga o Wi-Fi para o Mac escolher sozinho a rede preferida
    echo ">> Religando o Wi-Fi para voltar a rede preferida..."
    networksetup -setairportpower "$WIFI_IF" off; sleep 2; networksetup -setairportpower "$WIFI_IF" on
    for i in {1..15}; do tem_internet && break; sleep 1; done
  fi
  if tem_internet; then
    echo ">> Rede agora: $(rede_atual) (com internet)"
  else
    echo ">> AVISO: sem internet. Escolha a rede no menu Wi-Fi do Mac."
  fi
}

echo ">> 1/4 Compilando o APK..."
rm -f "$APK"   # garante que nao vamos instalar um APK velho se a compilacao falhar
./gradlew :TeamCode:assembleDebug -q 2>&1 | grep -vE "^WARNING|obsolete|Xlint|deprecated API|Recompile|^[0-9]+ warnings"
if [ "${pipestatus[1]}" -ne 0 ] || [ ! -f "$APK" ]; then
  echo "ERRO: a compilacao falhou. Veja as mensagens acima. Nada foi instalado."; exit 1
fi
echo ">> APK pronto: $APK ($(date -r "$APK" '+%H:%M:%S'))"

echo ">> 2/4 Conectando na rede do hub ($REDE_HUB)..."
networksetup -setairportnetwork "$WIFI_IF" "$REDE_HUB" "$SENHA_HUB"
ok=0
for i in {1..15}; do
  if ping -c 1 -W 1 "$IP_HUB" >/dev/null 2>&1; then ok=1; break; fi
  sleep 1
done
if [ $ok -ne 1 ]; then
  echo "ERRO: nao alcancei o hub em $IP_HUB. O Control Hub esta ligado?"
  volta_rede; exit 1
fi

echo ">> 3/4 Instalando no hub..."
"$ADB" connect "$IP_HUB:5555" >/dev/null 2>&1
if "$ADB" -s "$IP_HUB:5555" install -r "$APK" 2>&1 | grep -q Success; then
  echo ">> Instalado. Reiniciando o app do Robot Controller..."
  "$ADB" -s "$IP_HUB:5555" shell am start -n "$ACTIVITY" >/dev/null 2>&1
  sleep 3
  "$ADB" disconnect "$IP_HUB:5555" >/dev/null 2>&1
  RESULTADO="SUCESSO: app instalado no hub"
else
  RESULTADO="ERRO: a instalacao falhou (veja acima)"
fi

echo ">> 4/4"
volta_rede
echo ">> $RESULTADO"
