package org.scala_lang.index.sbt

import sbt._
import Keys._

/**
 * This is the scaladex publishing plugin which extends the main publish task
 * of SBT. Also the plugin does have some settings to configure scaladex.
 *
 * - scaladexKeywords: the keywords for scaladex artifact which is used in search
 * - scaladexDownloadReadme: Flag if scaladex have access to download the Readme from the repository
 * - scaladexDownloadContributors: Flag if scaladex have access to download the contributors from the repository
 * - scaladexDownloadInfo: Flag if scaladex have access to download the info from the repository (forks, stars, watches)
 */
object ScaladexPlugin extends AutoPlugin {

  object autoImport {

    /* defining the Scope Scaladex */
    lazy val Scaladex = config("scaladex") extend Compile
    lazy val scaladexKeywords = settingKey[Seq[String]]("list of keywords for your package in scaladex")
    lazy val scaladexDownloadReadme = settingKey[Boolean]("defines if scaladex should download the readme file from the defined scm tag")
    lazy val scaladexDownloadInfo = settingKey[Boolean]("defines if scaladex should download the project info from the defined scm tag")
    lazy val scaladexDownloadContributors = settingKey[Boolean]("defines if scaladex should download the contributors from the defined scm tag")
    lazy val scaladexBaseUrl = settingKey[String]("scaladex server location and path")

    /** define base scaladex options */
    lazy val baseScaladexSettings = Seq(
      scaladexKeywords := Seq(),
      scaladexDownloadContributors := true,
      scaladexDownloadInfo := true,
      scaladexDownloadReadme := true,
      scaladexBaseUrl := "https://index.scala-lang.org"
    ) ++
    inConfig(Scaladex)(
      /** import ivy publishing settings */
      Classpaths.ivyPublishSettings ++
      Classpaths.ivyBaseSettings ++
      Seq(
        publishTo := {

          /** prepare the url to publish on scaladex */
          val baseUrl = scaladexBaseUrl.value
          val basePath = "/publish?"

          val params = List(
            "readme" -> scaladexDownloadReadme.value,
            "info" -> scaladexDownloadInfo.value,
            "contributors" -> scaladexDownloadContributors.value,
            "path" -> "" // need to be at the end!
          )
          val keywords = scaladexKeywords.value.map(key => "keywords" -> key)

          val url = baseUrl + basePath + (keywords ++ params).map {

            case(k,v) => s"$k=$v"
          }.mkString("&")

          Some("Scaladex" at url)
        },
        publishArtifact in packageBin := true,
        publishArtifact in packageDoc := true,
        publishArtifact in packageSrc := true
      )
    ) ++ Seq(
      packagedArtifacts in Scaladex := (packagedArtifacts in Compile).value.filter(_._2.getName.endsWith(".pom"))
    )
  }

  import autoImport._
  override def trigger = allRequirements

  /** apply the settings */
  override lazy val projectSettings = baseScaladexSettings
}