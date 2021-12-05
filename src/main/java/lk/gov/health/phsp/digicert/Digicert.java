package lk.gov.health.phsp.digicert;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.enterprise.context.Dependent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jakarta.ws.rs.Path;
import lk.gov.health.phsp.entity.Encounter;
import lk.gov.health.phsp.facade.EncounterFacade;

@Path("")
@Dependent
public class Digicert extends HttpServlet {
    @EJB
    private EncounterFacade encounterFacade;

    public void doGet(HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
      PrintWriter printwriter = response.getWriter();
      response.setContentType("text/html");

      String id = request.getParameter("id");
      if (id == null || id.trim() == "") {
        response.setStatus(404);
        printwriter.println("Not Found");
      } else {
        Encounter test = encounterFacade.findByField("encounterIdHash", id, false);
        if (test == null) {
          response.setStatus(404);
          printwriter.println("not found");
        } else if (test.getResultPrintHtml() == null || test.getResultPrintHtml().trim() == "") {
          response.setStatus(404);
          printwriter.println("not found");
        } else {
          response.setStatus(200);
          printwriter.println("<html>");
          printwriter.println("<head>");
          printwriter.println("<style>");
          printwriter.println(".print-area { border: 1px solid #000; padding: 2cm; width: 210mm; height: 297mm; break-after: page; }");
          printwriter.println("</style>");
          printwriter.println("</head>");
          printwriter.println("<body>");
          printwriter.println("<div class='print-area'>");
          printwriter.println(test.getResultPrintHtml());
          printwriter.println("</div>");
          printwriter.println("</body>");
          printwriter.println("</html>");
        }
      }
      printwriter.close();
    }
}
