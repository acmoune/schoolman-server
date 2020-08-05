package utils

import akka.actor.ActorSystem
import com.amazonaws.auth._
import com.amazonaws.regions._
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest, PutObjectResult}
import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class S3Service @Inject()(config: Configuration, system: ActorSystem) {
  private implicit val awsExecutionContext: ExecutionContext = system.dispatchers.lookup("aws-context")
  private val clientRegion: Regions = Regions.DEFAULT_REGION
  private val accessKey: String = config.get[String]("aws.access.key")
  private val secretKey: String = config.get[String]("aws.secret.key")
  private val s3Bucket: String = config.get[String]("aws.s3.bucket")
  private val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)

  private val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withRegion(clientRegion)
    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
    .build()

  def uploadFile(file: java.io.File, fileName: String): Future[PutObjectResult] = {
    val req: PutObjectRequest = new PutObjectRequest(s3Bucket, fileName, file)
    req.withCannedAcl(CannedAccessControlList.PublicRead)
    Future { s3Client.putObject(req) }
  }

  def deleteFile(fileName: String): Future[Unit] =
    Future { s3Client.deleteObject(s3Bucket, fileName) }

  def getFileUrl(fileName: String): String =
    s"https://$s3Bucket.s3-${clientRegion.getName}.amazonaws.com/$fileName"
}
