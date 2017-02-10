java_plugin(
    name = "dagger_compiler",
    deps = [
        "@com_google_dagger_dagger//jar",
        "@com_google_dagger_dagger_compiler//jar",
        "@com_google_dagger_dagger_producers//jar",
        "@com_google_guava_guava//jar",
        "@javax_inject_javax_inject//jar",
    ],
    processor_class = "dagger.internal.codegen.ComponentProcessor",
)

android_binary(
    name = "cryptopass",
    custom_package = "krasilnikov.alexey.cryptopass",

    manifest = "src/main/AndroidManifest.xml",
    manifest_values = {},

    srcs = glob([
        "src/main/java/**",
    ]),
    resource_files = glob(["src/main/res/**"]),
    plugins = [
        ":dagger_compiler",
    ],
    deps = [
        "@com_google_dagger_dagger//jar",
        "@javax_inject_javax_inject//jar",
    ],
)


