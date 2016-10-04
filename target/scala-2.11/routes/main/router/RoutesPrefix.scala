
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/davide/GitHub/FlowUpServer/conf/routes
// @DATE:Tue Oct 04 11:10:51 CEST 2016


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
