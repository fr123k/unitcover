package integration

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.Play
import unit.org.stock.manager.test.DatabaseSetupBefore

import model.{DB, Build}
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

class BuildControllerSpec extends PlaySpecification with DatabaseSetupBefore {
	sequential
	implicit def toOption[A](value: A) : Option[A] = Some(value)
	implicit val timeout = 10000
	import DB.dal._
	import DB.dal.profile.simple._
	import model.JsonHelper._

	"one build exists in database" should {
		"POST to /api/<owner>/<project>/builds" should {
			"create a new build and return its buildNumber" in new WithServer { 
				val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").post(""))
				response.status must equalTo(OK)
				val buildNumber = (response.json \ "buildNumber").as[Int]
				DB.db withDynSession {
					checkBuild(buildNumber)
				}
			}

			"create two new builds and second build should have buildname equal three" in new WithServer { 
				val response1 = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").post(""))
				val response2 = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").post(""))
				response1.status must equalTo(OK)
				response2.status must equalTo(OK)
				val buildNumber1 = (response1.json \ "buildNumber").as[Int]
				val buildNumber2 = (response2.json \ "buildNumber").as[Int]
				DB.db withDynSession {
					checkBuild(buildNumber1)
					checkSecondBuild(buildNumber2)
				}
			}

			"upload complete build with two test suite results" in new WithServer {
				val karmaTestResult = scala.io.Source.fromFile(Play.getFile("test/resources/karma/test-results.xml")).mkString
				val sbtTestResultWithError = scala.io.Source.fromFile(Play.getFile("test/resources/sbt/TestSuiteControllerSpecError.xml")).mkString
				
				val buildResponse = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").post(""))
				buildResponse.status must equalTo(OK)
				val buildNumber = (buildResponse.json \ "buildNumber").as[Int]

				await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/$buildNumber").withHeaders("Content-Type" -> "text/xml").post(karmaTestResult))
				await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/$buildNumber").withHeaders("Content-Type" -> "text/xml").post(sbtTestResultWithError))
				val response2 = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds/$buildNumber/end").post(""))
				
				response2.status must equalTo(OK)
				DB.db withDynSession {
					val build = Builds.findByBuildNumber(buildNumber).firstOption.get
					build.buildNumber must beEqualTo(buildNumber)
					build.owner must beEqualTo("pussinboots")
					build.project must beEqualTo("bankapp")
					build.tests must beEqualTo(Some(9))
					build.failures must beEqualTo(Some(0))
					build.errors must beEqualTo(Some(1))
				}
			}
			

			"create a new build with trigger parameter" in new WithServer { 
				val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds?trigger=TravisCI").post(""))
				response.status must equalTo(OK)
				val buildNumber = (response.json \ "buildNumber").as[Int]
				DB.db withDynSession {
					checkBuildTravisCI(buildNumber)
				}
			}
		

			"create a new build with travisBuildId parameter" in new WithServer { 
				val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds?travisBuildId=123456").post(""))
				response.status must equalTo(OK)
				val buildNumber = (response.json \ "buildNumber").as[Int]
				DB.db withDynSession {
					checkBuildTravisBuildId(buildNumber)
				}
			}
		
			"create a new build with trigger and branch parameter" in new WithServer { 
				val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds?trigger=TravisCI&branch=notMaster").post(""))
				response.status must equalTo(OK)
				val buildNumber = (response.json \ "buildNumber").as[Int]
				DB.db withDynSession {
					checkBuildTravisCINotMaster(buildNumber)
				}
			}
			
			"build limit" should {
				"enabled with value two" should{
					"create four new builds and delete old once and all it testsuites except the latest two's" in new WithServer(app = FakeApplication(additionalConfiguration=Map("buildslimit" -> "2"))) {
						uploadFourBuilds(port)
						//check that the three old builds and complete test suites and cases are deleted
						DB.db withDynSession {
							testSuiteNotExists(1)
							testSuiteNotExists(2)
							testSuiteNotExists(3)
						}
						val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").get)
						response.status must equalTo(OK)
						val builds = Json.fromJson[JsonFmtListWrapper[Build]](response.json).get
						builds.count must equalTo(2)
						builds.items.length must equalTo(2)
						builds.items(0).buildNumber must equalTo(5)
						builds.items(1).buildNumber must equalTo(4)
					}
				}

				"disabled with value zero" should{
					"create four new builds and nothing should be deleted five builds should be left" in new WithServer(app = FakeApplication(additionalConfiguration=Map("buildslimit" -> "0"))) {
						uploadFourBuilds(port)
						val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").get)
						response.status must equalTo(OK)
						val builds = Json.fromJson[JsonFmtListWrapper[Build]](response.json).get
						builds.count must equalTo(5)
						builds.items.length must equalTo(5)
						builds.items(0).buildNumber must equalTo(5)
						builds.items(1).buildNumber must equalTo(4)
						builds.items(2).buildNumber must equalTo(3)
						builds.items(3).buildNumber must equalTo(2)
						builds.items(4).buildNumber must equalTo(1)
					}
				}
			}
		}

		"GET to /api/<owner>/<project>/builds" should {
			"return latest 10 builds" in new WithServer {
				insert10Builds()
				val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").get)
				response.status must equalTo(OK)
				val builds = Json.fromJson[JsonFmtListWrapper[Build]](response.json).get
				builds.count must equalTo(12)
				builds.items.length must equalTo(10)
				builds.items(0).buildNumber must equalTo(12)
			}
		}
	}

	//given

	def insert10Builds() {
		DB.db withDynSession {
			for(i <- 1 to 11)
				Builds.insertAndIncrement("pussinboots", "bankapp")
		}	
	}

	def uploadFourBuilds(port: Int) {
		import play.api.Play.current
		for(i <- 1 to 4) {
			val response = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds").post(""))
			response.status must equalTo(OK)
			val buildNumber = (response.json \ "buildNumber").as[Int]
			val sbtTestResultWithError = scala.io.Source.fromFile(Play.getFile("test/resources/sbt/TestSuiteControllerSpecError.xml")).mkString
			val testSuiteResponse = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/$buildNumber").withHeaders("Content-Type" -> "text/xml").post(sbtTestResultWithError))
			testSuiteResponse.status must equalTo(OK)
			val suiteId = (testSuiteResponse.json \ "testsuites"  \\ "id").map(_.as[Int]).head
			val responseEndBuild = await(WS.url(s"http://localhost:$port/api/pussinboots/bankapp/builds/$buildNumber/end").post(""))
			responseEndBuild.status must equalTo(OK)
			DB.db withDynSession {
				checkTestSuiteWithError(suiteId, buildNumber)
				checkTestCasesWithError(suiteId)
			}

		}
	}

	//then
	def checkBuildTravisCINotMaster(buildId: Int) {
		val build = Builds.findByBuildNumber(buildId).firstOption.get
		build.buildNumber must beEqualTo(2)
		build.owner must beEqualTo("pussinboots")
		build.project must beEqualTo("bankapp")
		build.trigger must beEqualTo(Some("TravisCI"))
		build.branch must beEqualTo(Some("notMaster"))
	}

	def checkBuildTravisCI(buildId: Int) {
		val build = Builds.findByBuildNumber(buildId).firstOption.get
		build.buildNumber must beEqualTo(2)
		build.owner must beEqualTo("pussinboots")
		build.project must beEqualTo("bankapp")
		build.trigger must beEqualTo(Some("TravisCI"))
	}

	def checkBuildTravisBuildId(buildId: Int) {
		val build = Builds.findByBuildNumber(buildId).firstOption.get
		build.buildNumber must beEqualTo(2)
		build.owner must beEqualTo("pussinboots")
		build.project must beEqualTo("bankapp")
		build.travisBuildId must beEqualTo(Some("123456"))
	}

    def checkBuild(buildId: Int) {
		val build = Builds.findByBuildNumber(buildId).firstOption.get
		build.buildNumber must beEqualTo(2)
		build.owner must beEqualTo("pussinboots")
		build.project must beEqualTo("bankapp")
	}

	def checkSecondBuild(buildId: Int) {
		val build = Builds.findByBuildNumber(buildId).firstOption.get
		build.buildNumber must beEqualTo(3)
		build.owner must beEqualTo("pussinboots")
		build.project must beEqualTo("bankapp")
	}

	def checkTestSuiteWithError(suiteId: Long, buildNumber: Int) {
		val suite = findById(suiteId).firstOption.get
		suite.id must beEqualTo(Some(suiteId))
		suite.buildNumber must beEqualTo(buildNumber)
		suite.owner must beEqualTo("pussinboots")
		suite.project must beEqualTo("bankapp")
		suite.name must beEqualTo("integration.TestSuiteControllerSpec")
		suite.tests must beEqualTo(Some(3))
		suite.failures must beEqualTo(Some(0))
		suite.errors must beEqualTo(Some(1))
		suite.duration must beEqualTo(Some(14.179))
	}
	def checkTestCasesWithError(suiteId: Long) {
		val testCases = findBySuite(suiteId).sortBy(_.id.asc).list()
		testCases.length must equalTo(3)
		testCases(0).name must equalTo("POST to /api/<owner>/<project> should::with junit xml report all tests passed return http status 200 and store it in the db")
		testCases(0).typ must equalTo(Some("java.lang.Exception"))
		testCases(0).errorMessage must equalTo(Some("test error"))
		testCases(1).name must equalTo("GET to /api/<owner>/<project> should::return latest 10 test suites")
		testCases(1).failureMessage must equalTo(None)
		testCases(2).name must equalTo("POST to /api/<owner>/<project> should::with junit xml report one failure return http status 200 and store it in the db")
		testCases(2).failureMessage must equalTo(None)
	}

	def testSuiteNotExists(buildNumber: Int) {
		val deletedTestCases = findBySuite(buildNumber).list()
		println(deletedTestCases)
		deletedTestCases.length must equalTo(0)
		val deletedTestSuites = findResultsBy("pussinboots", "bankapp", buildNumber).list()
		deletedTestSuites.length must equalTo(0)
	}
}
