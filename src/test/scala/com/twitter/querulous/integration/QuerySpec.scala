package com.twitter.querulous.integration

import org.specs.Specification
import net.lag.configgy.Configgy
import com.twitter.xrayspecs.Time
import com.twitter.xrayspecs.TimeConversions._
import com.twitter.querulous.database.ApachePoolingDatabaseFactory
import com.twitter.querulous.query._
import com.twitter.querulous.evaluator.{StandardQueryEvaluatorFactory, QueryEvaluator}


class QuerySpec extends Specification {
  Configgy.configure("config/" + System.getProperty("stage", "test") + ".conf")

  import TestEvaluator._
  val config = Configgy.config.configMap("db")

  "Query" should {
    val queryEvaluator = testEvaluatorFactory(config)

    "with too many arguments" >> {
      queryEvaluator.select("SELECT 1 FROM DUAL WHERE 1 IN (?)", 1, 2, 3) { r => 1 } must throwA[TooManyQueryParametersException]
    }

    "with too few arguments" >> {
      queryEvaluator.select("SELECT 1 FROM DUAL WHERE 1 = ? OR 1 = ?", 1) { r => 1 } must throwA[TooFewQueryParametersException]
    }

    "with just the right number of arguments" >> {
      queryEvaluator.select("SELECT 1 FROM DUAL WHERE 1 IN (?)", List(1, 2, 3))(_.getInt(1)).toList mustEqual List(1)
    }

    "be backwards compatible" >> {
      val noOpts = testEvaluatorFactory("localhost", null, config("username"), config("password"))
      noOpts.select("SELECT 1 FROM DUAL WHERE 1 IN (?)", List(1, 2, 3))(_.getInt(1)).toList mustEqual List(1)

      val noDBNameOrOpts = testEvaluatorFactory("localhost", config("username"), config("password"))
      noDBNameOrOpts.select("SELECT 1 FROM DUAL WHERE 1 IN (?)", List(1, 2, 3))(_.getInt(1)).toList mustEqual List(1)
    }
  }
}
