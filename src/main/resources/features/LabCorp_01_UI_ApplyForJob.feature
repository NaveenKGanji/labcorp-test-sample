@TEST
@LABCORP_UI_01
@LABCORP_TEST

Feature: Navigate and Apply for the QA Test Automation Developer Position

  Scenario Outline: Navigate and Apply for the QA Test Automation Developer Position
    Given the applicant launches LabCorp and Navigate to Career Page
    And the applicant searches and selects the position "QA Test Automation Developer"
    And the applicant verifies the below job information:
      | <Job_Title> | <Job_Location> | <Job_ID> | <Requirement_Text> |
    And the applicant applies for the job and confirms the following information:
      | <Job_Title> | <Job_Location> | <Job_ID> | <Schedule_Text> |
    When the applicant clicks on Return to Job Search
    Then the applicant is navigated to Job Search Page

    Examples:
      | Job_Title                              | Job_Location           | Job_ID   | Requirement_Text     | Schedule_Text |
      | QA Test Automation Developer/Architect | Durham, North Carolina | 21-87609 | Computer Engineering | 40 hrs/wk     |