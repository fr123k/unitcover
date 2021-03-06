package test

import model.{DateUtil, DB, TestSuite, TestCase, Message, Build}
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

object SetupTestDatabase {
  implicit def toOption[A](value: A) : Option[A] = Some(value)

  var now = DateUtil.nowDateTime()
  var yesterday = Option(DateUtil.daysBeforDateTime(1))
  import DB.dal
  import dal._
  import DB.dal.profile.simple._
  def insertTestData(googleId: String = "test googleId") = {
    DB.dal.recreate

    Builds.builds.insert(Build(owner="pussinboots", project="bankapp", buildNumber=1, date=now, tests=Some(5),failures = Some(1), errors = Some(2), travisBuildId=Some("1")))
    dal.testSuites.insert(TestSuite(None, 1, "pussinboots", "bankapp", "testsuite", 5,1,2,1000.0, now))
    dal.testCases.insert(TestCase(None, 1, "pussinboots", "bankapp", "testcase", "testclass",1000.0))
    (now, yesterday)
  }

  //test data used by end 2 end test with karma
  def insertE2ETestData(googleId: String = "test googleId") = {
    insert10TestSuites()
    (now, yesterday)
  }

  def insert10TestSuites() {
    val owner ="pussinboots"
    val project = "bankapp"
    for(i <- 2 to 11)
      Builds.builds.insert(Build(buildNumber=i, owner=owner, project=project, tests=i, failures=i/8, errors=i/10, travisBuildId=Some(s"$i")))
    
    Builds.builds.insert(Build(buildNumber=1, owner="otherowner", project="otherproject", tests=20, failures=0, errors=1, travisBuildId=Some(s"1")))

    for(i <- 2 to 7) {
      val testSuite = dal.testSuiteForInsert.insert(TestSuite(None, i, owner, project, s"testsuite $i", 8,0,0,1000.0, now))
      dal.testCaseForInsert.insert(TestCase(None, testSuite.id.get, "pussinboots", "bankapp", s"testcase $i ${testSuite.name}", "testclass",1000.0))
      val testCase = dal.testCaseForInsert.insert(TestCase(None, testSuite.id.get, "pussinboots", "bankapp", s"testcase $i ${testSuite.name} failure", "testclass",1000.0))      
    }
    for(i <- 8 to 9) {
      val testSuite = dal.testSuiteForInsert.insert(TestSuite(None, i, owner, project, s"testsuite $i", 8,1,0,1000.0, now))
      dal.testCaseForInsert.insert(TestCase(None, testSuite.id.get, "pussinboots", "bankapp", s"testcase $i ${testSuite.name}", "testclass",1000.0))
      var testCase = dal.testCaseForInsert.insert(TestCase(None, testSuite.id.get, "pussinboots", "bankapp", s"testcase $i ${testSuite.name} failure", "testclass",1000.0, Some("failureMessage")))
      dal.messages.insert(Message(testCase.id.get, s"failure message $i", 0))
    }

    for(i <- 10 to 11) {
      val testSuite = dal.testSuiteForInsert.insert(TestSuite(None, i, owner, project, s"testsuite $i", 8,1,1,1000.0, now))
      dal.testCaseForInsert.insert(TestCase(None, testSuite.id.get, "pussinboots", "bankapp", s"testcase $i ${testSuite.name}", "testclass",1000.0))
      var testCase = dal.testCaseForInsert.insert(TestCase(None, testSuite.id.get, "pussinboots", "bankapp", s"testcase $i ${testSuite.name} failure", "testclass",1000.0, Some("failureMessage")))
      dal.messages.insert(Message(testCase.id.get, s"failure message $i", 0))
      testCase = dal.testCaseForInsert.insert(TestCase(None, testSuite.id.get, "pussinboots", "bankapp", s"testcase $i ${testSuite.name} error", "testclass",1000.0, errorMessage=Some("errorMessage")))
      dal.messages.insert(Message(testCase.id.get, s"error message $i", 1))
    }
  }
}
