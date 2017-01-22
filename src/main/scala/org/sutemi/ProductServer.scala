package org.sutemi

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

object ProductServerMain extends ProductServer

class ProductServer extends HttpServer {

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[ProductController]
  }

}


class ProductController extends Controller {

  get("/hi") { request: Request =>
    info("hi")
    "Hello " + request.params.getOrElse("name", "unnamed")
  }

  get("/") { request: Request =>
    response.ok.file("/graphiql.html")
  }


}