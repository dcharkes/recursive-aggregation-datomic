recursive-aggregation-datomic
=============================

A prototype application to explore the recursive aggregation capabilities and performance of Datomic, written in Clojure.



### Application Data Model

![Application Data Model](/doc/grading-entities.png "Application Data Model")

Declarative specification of derivations:

  *	Submission.grade is derived from Submission.data by a function
  *	Assignment.meanGrade is avg(Submission.grade) for all submissions which are related to the specific assignment
  *	Unit.meanGrade is
    *	avg(Assignment.meanGrade) if there are any assignments related to this unit
    *	avg(Child Unit.meanGrade) else
    *	StudentAssignment is the cross product of all Assignments and Students.
  *	StudentAssignment.grade is
    *	Submission.grade if there exists a Submission for the specific student and assignment
    *	0.0 else
  *	StudentUnit is the cross product of all Units and Students
  *	StudentUnit.meanGrade is
    *	avg(StudentAssignment.grade) if there are any StudentAssignments related to this StudentUnit
    *	avg(Child StudentUnit.meanGrade) else

These are recursive aggregations. A combination of aggregation (through averaging) and recursive definitions (the tree structure of units).

All dotted elements in the diagram are derived. These should be specified declaratively, and the programming platform should provide “materialized views” and incremental updates for performance.

