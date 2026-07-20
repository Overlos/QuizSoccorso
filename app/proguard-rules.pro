# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\andre\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# Add any project specific keep options here:

# Gson-specific rules to handle minification safely
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Prevent Gson from obfuscating the fields in our data models
# This is safe because we also use @SerializedName, but keep rules ensure
# constructors and fields are available for reflection if needed.
-keep class com.example.quizsoccorso.QuizQuestion { *; }
-keep class com.example.quizsoccorso.QuestionStat { *; }

# Gson standard keep rules
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.TypeAdapter
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer

# Handle generic type information for List<QuizQuestion> and Map<Int, QuestionStat>
-keep class com.google.gson.internal.LinkedTreeMap { *; }
