# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 스택트레이스에서 파일명, 줄번호 보이게 하기
-keepattributes SourceFile,LineNumberTable

# QRType, ScanSource enum - Room DB에 이름 문자열로 저장되므로
# obfuscation 하면 valueOf()로 읽을 때 전부 UNKNOWN으로 떨어짐
-keep enum com.peanutbutter1001.qron.domain.model.** { *; }