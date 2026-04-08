# Add project specific ProGuard rules here.
# You can control the set of files you want to keep by specifying a list of rules.
# You can then specify which rules to apply to specific files or classes.

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Hilt and Dagger generated classes
-keep class com.alarmed.app.di.** { *; }
-keep class dagger.hilt.** { *; }
-keep class androidx.hilt.** { *; }
