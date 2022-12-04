package info.hannes.github

private fun String.toHttps() = replace("git@", "https//")
    .replace(":", "/")
    .replace("///", "//")
    .replace("https//", "https://").split("/")

fun String.user() = this.toHttps()[3]
fun String.repo() = this.toHttps()[4]