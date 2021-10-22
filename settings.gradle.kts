dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven {
//            url=uri("https://maven.aliyun.com/nexus/content/groups/public/")
//        }
        google()
        mavenCentral()
    }
}
rootProject.name = "npugpa"
include(":app")
 