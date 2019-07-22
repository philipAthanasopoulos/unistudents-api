package com.unistudents.api.parser;

import com.unistudents.api.model.Course;
import com.unistudents.api.model.GradeResults;
import com.unistudents.api.model.Semester;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StudentsParser {

    private Document gradesPage;

    public StudentsParser() {
    }

    public StudentsParser(Document gradesPage) {
        this.gradesPage = gradesPage;
    }

    public GradeResults getResults() {

        Element elements = gradesPage.getElementById("mainTable");
        Elements table = elements.getElementsByAttributeValue("cellspacing", "0");

        GradeResults results = new GradeResults();
        Semester semesterObj = null;
        Course courseObj = null;

        for (Element element : table.select("tr")) {

            // get new semester
            Elements semester = element.select("td.groupheader");
            if (semester != null) {
                if (!semester.text().equals("")) {
                    System.out.println(". " + semester.text());
                    semesterObj = new Semester();

                    // set semester id
                    int id = Integer.parseInt(semester.text().substring(semester.text().length() - 1));
                    semesterObj.setId(id);
                }
            }

            // get courses
            Elements course = element.getElementsByAttributeValue("bgcolor", "#fafafa");
            if (course != null) {
                if (!course.text().equals("")) {
                    int counter = 0;
                    for (Element courseElement : course.select("td")) {
                        counter++;

                        // get course id & name
                        Elements courseName = courseElement.getElementsByAttributeValue("colspan", "2");
                        if (courseName != null) {
                            if (!courseName.text().equals("")) {
                                courseObj = new Course();
                                String name = courseName.text();
                                System.out.println("Course: " + name);
                                courseObj.setName(name.substring(name.indexOf(") ") + 2));
                                courseObj.setId(name.substring(name.indexOf("(")+1,name.indexOf(")")));
                            }
                        }

                        if (counter == 3) {
                            courseObj.setType(courseElement.text());
                        }
                        else if (counter == 7) {
                            if (!courseElement.text().equals("-"))
                                courseObj.setGrade(Double.parseDouble(courseElement.text()));
                        }
                        else if (counter == 8) {
                            courseObj.setExamPeriod(courseElement.text());
                        }
                    }
                    semesterObj.getCourses().add(courseObj);
                }
            }

            // get final info & add semester obj
            Elements finalInfo = element.select("tr.subHeaderBack");
            if (finalInfo != null) {
                if (!finalInfo.text().equals("")) {


                    for (Element finalInfoEl : finalInfo) {

                        // get total passed courses
                        Elements elPassesCourses = finalInfoEl.getElementsByAttributeValue("colspan", "3");
                        if (elPassesCourses != null) {
                            if (results.getSemesters().contains(semesterObj)) {
                                results.setTotalPassedCourses(Integer.parseInt(elPassesCourses.text().substring(elPassesCourses.text().length() - 2)));
                            ***REMOVED***
                                System.out.println("Passed Courses: " + elPassesCourses.text().substring(elPassesCourses.text().length() - 1));
                                semesterObj.setPassedCourses(Integer.parseInt(elPassesCourses.text().substring(elPassesCourses.text().length() - 1)));
                            }
                        }

                        // get semester avg
                        Elements tableCell = finalInfoEl.getElementsByAttributeValue("colspan", "10");
                        if (tableCell != null) {
                            int counter = 0;
                            for (Element el : tableCell.select(".error")) {
                                counter++;
                                if (counter == 1) {
                                    if (results.getSemesters().contains(semesterObj)) {
                                        results.setTotalAverageGrade(Double.parseDouble(el.text().replace("-","")));
                                    }
                                    else {
                                        semesterObj.setGradeAverage(Double.parseDouble(el.text()));
                                    }
                                }
                                else if (counter == 4) {
                                    if (results.getSemesters().contains(semesterObj)) {
                                        results.setTotalEcts(Integer.parseInt(el.text()));
                                    }
                                    else {
                                        semesterObj.setEcts(Integer.parseInt(el.text()));
                                    }
                                }
                            }
                        }
                    }

                    // add semesterObj to resultsObj
                    if (!results.getSemesters().contains(semesterObj))
                        results.getSemesters().add(semesterObj);
                }
            }
        }
        return results;
    }
}
