package org.vrp.utils.Models;

import org.vrp.utils.meta.RjsonArray;

import java.lang.annotation.Retention;
import java.util.List;

public class SampleArray {

    String name;
    String gender;
    int age;
    @RjsonArray( type="java.lang.String")
    List<String> languages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
}
