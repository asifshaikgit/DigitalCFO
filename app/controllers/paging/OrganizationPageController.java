package controllers.paging;

import controllers.BaseController;
import controllers.StaticController;
import model.Organization;
import play.data.Form;

import javax.transaction.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.html.organization.*;
import pojo.OrganiationPage;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
// import static play.data.Form;

/**
 * Created by Sunil Namdev on 21-09-2018.
 */
public class OrganizationPageController extends BaseController {
    private EntityManager entityManager;

    public Result GO_HOME = redirect(
            routes.OrganizationPageController.list(0, "createdAt", "desc", ""));

    public Result index() {
        return GO_HOME;
    }

    @Transactional
    public Result list(int page, String sortBy, String order, String filter) {
        OrganiationPage organization = Organization.page(entityManager, page, 10, sortBy, order, filter);
        return Results.ok(organizationList.render(organization, sortBy, order, filter));
    }
}
