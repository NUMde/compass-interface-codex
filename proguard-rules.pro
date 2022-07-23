-keep class MainKt { *;  }

# HAPI
-keep class ca.uhn.fhir.context.FhirVersionEnum { *;  }
-keep class org.hl7.fhir.r4.hapi.ctx.FhirR4 { *;  }
-keep class org.hl7.fhir.r4.model.* { }
-keep enum org.apache.http.client.utils.URIUtils$UriFlag { *; }
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# kotlin-reflect
-keep class kotlin.Metadata { *; }
-keep interface kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoader
-keep class * implements kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoader { public protected *; }
-keep interface kotlin.reflect.jvm.internal.impl.resolve.ExternalOverridabilityCondition
-keep class * implements kotlin.reflect.jvm.internal.impl.resolve.ExternalOverridabilityCondition { public protected *; }
-keepattributes InnerClasses,Signature,RuntimeVisible*Annotations,EnclosingMethod


# kotlin-serialization
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault


-keep class CompassDownloaderKt { *; } # keep proguardKeep() method with serializers TODO somehow the other kotlin-serialization rules do not work


-ignorewarnings
-dontobfuscate
-dontoptimize
-dontwarn

