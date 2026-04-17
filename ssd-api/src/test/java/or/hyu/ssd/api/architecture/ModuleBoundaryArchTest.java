package or.hyu.ssd.api.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "or.hyu.ssd")
class ModuleBoundaryArchTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_api_auth_external_or_infra =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "or.hyu.ssd.document..",
                            "or.hyu.ssd.member..",
                            "or.hyu.ssd.ai..",
                            "or.hyu.ssd.shared.."
                    )
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "or.hyu.ssd.api..",
                            "or.hyu.ssd.auth..",
                            "or.hyu.ssd.external..",
                            "or.hyu.ssd.infra.."
                    );

    @ArchTest
    static final ArchRule common_must_not_depend_on_other_project_modules =
            noClasses()
                    .that()
                    .resideInAnyPackage("or.hyu.ssd.common..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "or.hyu.ssd.api..",
                            "or.hyu.ssd.auth..",
                            "or.hyu.ssd.external..",
                            "or.hyu.ssd.infra..",
                            "or.hyu.ssd.document..",
                            "or.hyu.ssd.member..",
                            "or.hyu.ssd.ai..",
                            "or.hyu.ssd.shared.."
                    );

    @ArchTest
    static final ArchRule auth_must_not_depend_on_api_or_infra =
            noClasses()
                    .that()
                    .resideInAnyPackage("or.hyu.ssd.auth..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("or.hyu.ssd.api..", "or.hyu.ssd.infra..");

    @ArchTest
    static final ArchRule external_must_not_depend_on_api_auth_domain_or_infra =
            noClasses()
                    .that()
                    .resideInAnyPackage("or.hyu.ssd.external..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "or.hyu.ssd.api..",
                            "or.hyu.ssd.auth..",
                            "or.hyu.ssd.document..",
                            "or.hyu.ssd.member..",
                            "or.hyu.ssd.ai..",
                            "or.hyu.ssd.shared..",
                            "or.hyu.ssd.infra.."
                    );

    @ArchTest
    static final ArchRule api_contracts_must_not_depend_on_infra_or_external =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "or.hyu.ssd.api..controller..",
                            "or.hyu.ssd.api..request..",
                            "or.hyu.ssd.api..response.."
                    )
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("or.hyu.ssd.infra..", "or.hyu.ssd.external..");

    @ArchTest
    static final ArchRule infra_persistence_must_not_depend_on_api_auth_or_external =
            noClasses()
                    .that()
                    .resideInAnyPackage("or.hyu.ssd.infra.persistence..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("or.hyu.ssd.api..", "or.hyu.ssd.auth..", "or.hyu.ssd.external..");
}
