/*
  iDmc Nullsoft Scriptable Install System 2 script for Windoze.
  Written by Daniele Pizzoni <auouo@tin.it>
*/

; HM NIS Edit Wizard helper defines
!define APP_NAME "iDmc"
!define APP_VERSION "2.0.5"
!define LIB_VERSION "0.7.0"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "README"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"

; MUI end ------

!define APP_FULLNAME "${APP_NAME}-${APP_VERSION}"
!define LIB_FULLNAME "jidmclib"
!define FILE_JAR "${APP_FULLNAME}.jar"
!define FILE_DLL "${LIB_FULLNAME}.dll"
!define DIR_MODELS "$INSTDIR\models"


Name "${APP_FULLNAME}"
OutFile "${APP_FULLNAME}-setup.exe"
InstallDir "$PROGRAMFILES\${APP_FULLNAME}"
ShowInstDetails show
ShowUnInstDetails show
AutoCloseWindow false

Section "Executables" SEC01
    SetOutPath "$INSTDIR"
    SetOverwrite on

    File "${FILE_JAR}"
    File "${FILE_DLL}"
    File README
    File COPYING
SectionEnd

Section "Models" SEC02
    SetOutPath "${DIR_MODELS}"
    SetOverwrite on

    File "models\cremona.lua"
    File "models\ctbif.lua"
    File "models\ctlocal.lua"
    File "models\gingerman.lua"
    File "models\henon.lua"
    File "models\henon2.lua"
    File "models\hopf.lua"
    File "models\ikeda.lua"
    File "models\logist.lua"
    File "models\lorenz.lua"
    File "models\lv.lua"
    File "models\nordmark.lua"
    File "models\par.lua"
    File "models\quasi.lua"
    File "models\quasiperiodicity.lua"
    File "models\rossler.lua"
    File "models\rotor.lua"
    File "models\silnikov2.lua"
    File "models\standard.lua"
    File "models\tent.lua"
    File "models\tinkerbell.lua"
SectionEnd

Section "Links"
  CreateDirectory "$SMPROGRAMS\${APP_FULLNAME}"
  CreateShortCut "$SMPROGRAMS\${APP_FULLNAME}\${APP_FULLNAME}.lnk" "$INSTDIR\${FILE_JAR}" -library="$INSTDIR\${FILE_DLL}"
  CreateShortCut "$DESKTOP\${APP_FULLNAME}.lnk" "$INSTDIR\${FILE_JAR}" -library="$INSTDIR\${FILE_DLL}"
  CreateShortCut "$DESKTOP\iDmc models.lnk" "${DIR_MODELS}"
  CreateShortCut "$SMPROGRAMS\${APP_FULLNAME}\Uninstall.lnk" "$INSTDIR\uninst.exe"
SectionEnd

Section "Registry"
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_FULLNAME}" "DisplayName" "$(^Name)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_FULLNAME}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_FULLNAME}" "DisplayVersion" "${APP_VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_FULLNAME}" "Publisher" "TshoSoft"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_FULLNAME}" "InstallLocation" "$INSTDIR"
SectionEnd


Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "${APP_FULLNAME} was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove ${APP_FULLNAME} and all of its components?" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\uninst.exe"

  Delete "${DIR_MODELS}\cremona.lua"
  Delete "${DIR_MODELS}\ctbif.lua"
  Delete "${DIR_MODELS}\ctlocal.lua"
  Delete "${DIR_MODELS}\gingerman.lua"
  Delete "${DIR_MODELS}\henon.lua"
  Delete "${DIR_MODELS}\henon2.lua"
  Delete "${DIR_MODELS}\hopf.lua"
  Delete "${DIR_MODELS}\ikeda.lua"
  Delete "${DIR_MODELS}\logist.lua"
  Delete "${DIR_MODELS}\lorenz.lua"
  Delete "${DIR_MODELS}\lv.lua"
  Delete "${DIR_MODELS}\nordmark.lua"
  Delete "${DIR_MODELS}\par.lua"
  Delete "${DIR_MODELS}\quasi.lua"
  Delete "${DIR_MODELS}\quasiperiodicity.lua"
  Delete "${DIR_MODELS}\rossler.lua"
  Delete "${DIR_MODELS}\rotor.lua"
  Delete "${DIR_MODELS}\silnikov2.lua"
  Delete "${DIR_MODELS}\standard.lua"
  Delete "${DIR_MODELS}\tent.lua"
  Delete "${DIR_MODELS}\tinkerbell.lua"

  RmDir "${DIR_MODELS}"
  
  Delete "$INSTDIR\${FILE_DLL}"
  Delete "$INSTDIR\${FILE_JAR}"

  Delete "$SMPROGRAMS\${APP_FULLNAME}\Uninstall.lnk"
  Delete "$SMPROGRAMS\${APP_FULLNAME}\${APP_FULLNAME}.lnk"
  Delete "$DESKTOP\${APP_FULLNAME}.lnk"
  Delete "$DESKTOP\iDmc models.lnk"

  Delete COPYING
  Delete README

  RMDir "$SMPROGRAMS\${APP_FULLNAME}"
  RMDir "$INSTDIR"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_FULLNAME}"
SectionEnd

