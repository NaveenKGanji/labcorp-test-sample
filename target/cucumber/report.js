$(document).ready(function() {var formatter = new CucumberHTML.DOMFormatter($('.cucumber-report'));formatter.uri("src/main/resources/features/01_Login-LapCorp.feature");
formatter.feature({
  "name": "Login into UI and Apply for the QA Test Automation Developer Position",
  "description": "",
  "keyword": "Feature",
  "tags": [
    {
      "name": "@TEST"
    },
    {
      "name": "@UI_01"
    },
    {
      "name": "@LABCORP_TEST"
    }
  ]
});
formatter.scenarioOutline({
  "name": "Login into UI and Apply for the QA Test Automation Developer Position",
  "description": "",
  "keyword": "Scenario Outline"
});
formatter.step({
  "name": "the applicant launches LabCorp and Navigate to Career Page",
  "keyword": "Given "
});
formatter.step({
  "name": "the applicant searches and selects the position \"QA Test Automation Developer\"",
  "keyword": "And "
});
formatter.step({
  "name": "the applicant verifies the below job information:",
  "keyword": "And ",
  "rows": [
    {
      "cells": [
        "\u003cJob_Title\u003e",
        "\u003cJob_Location\u003e",
        "\u003cJob_ID\u003e",
        "\u003cRequirement_Text\u003e"
      ]
    }
  ]
});
formatter.step({
  "name": "the applicant applies for the job and confirms the following information:",
  "keyword": "And ",
  "rows": [
    {
      "cells": [
        "\u003cJob_Title\u003e",
        "\u003cJob_Location\u003e",
        "\u003cJob_ID\u003e",
        "\u003cSchedule_Text\u003e"
      ]
    }
  ]
});
formatter.step({
  "name": "the applicant clicks on Return to Job Search",
  "keyword": "When "
});
formatter.step({
  "name": "the applicant is navigated to Job Search Page",
  "keyword": "Then "
});
formatter.examples({
  "name": "",
  "description": "",
  "keyword": "Examples",
  "rows": [
    {
      "cells": [
        "Job_Title",
        "Job_Location",
        "Job_ID",
        "Requirement_Text",
        "Schedule_Text"
      ]
    },
    {
      "cells": [
        "QA Test Automation Developer/Architect",
        "Durham, North Carolina",
        "21-87609",
        "Computer Engineering",
        "40 hrs/wk"
      ]
    }
  ]
});
formatter.scenario({
  "name": "Login into UI and Apply for the QA Test Automation Developer Position",
  "description": "",
  "keyword": "Scenario Outline",
  "tags": [
    {
      "name": "@TEST"
    },
    {
      "name": "@UI_01"
    },
    {
      "name": "@LABCORP_TEST"
    }
  ]
});
formatter.before({
  "status": "passed"
});
formatter.step({
  "name": "the applicant launches LabCorp and Navigate to Career Page",
  "keyword": "Given "
});
formatter.match({
  "location": "MyStepdefs.theUserLogsIntoLabCorpAndNavigateToCareerPage()"
});
formatter.result({
  "status": "passed"
});
formatter.step({
  "name": "the applicant searches and selects the position \"QA Test Automation Developer\"",
  "keyword": "And "
});
formatter.match({
  "location": "MyStepdefs.theUserSearchesAndSelectsThePosition(String)"
});
formatter.result({
  "status": "passed"
});
formatter.step({
  "name": "the applicant verifies the below job information:",
  "rows": [
    {
      "cells": [
        "QA Test Automation Developer/Architect",
        "Durham, North Carolina",
        "21-87609",
        "Computer Engineering"
      ]
    }
  ],
  "keyword": "And "
});
formatter.match({
  "location": "MyStepdefs.theUserValidatesTheBelowJobInformation(String\u003e)"
});
formatter.result({
  "status": "passed"
});
formatter.step({
  "name": "the applicant applies for the job and confirms the following information:",
  "rows": [
    {
      "cells": [
        "QA Test Automation Developer/Architect",
        "Durham, North Carolina",
        "21-87609",
        "40 hrs/wk"
      ]
    }
  ],
  "keyword": "And "
});
formatter.match({
  "location": "MyStepdefs.theApplicantAppliesForTheJobAndConfirmsTheFollowingInformation(String\u003e)"
});
formatter.result({
  "status": "passed"
});
formatter.step({
  "name": "the applicant clicks on Return to Job Search",
  "keyword": "When "
});
formatter.match({
  "location": "MyStepdefs.theApplicantClicksOnReturnToJobSearch()"
});
formatter.result({
  "status": "passed"
});
formatter.step({
  "name": "the applicant is navigated to Job Search Page",
  "keyword": "Then "
});
formatter.match({
  "location": "MyStepdefs.theApplicantIsNavigatedToJobSearchPage()"
});
formatter.result({
  "status": "passed"
});
});