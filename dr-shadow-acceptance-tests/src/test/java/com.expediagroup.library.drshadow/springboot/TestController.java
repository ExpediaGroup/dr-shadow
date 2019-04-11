package com.expediagroup.library.drshadow.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

@Controller
@RequestMapping(value="/", produces= MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
public class TestController {

    @RequestMapping(value = {"/get"}, method = RequestMethod.GET)
    @ResponseBody
    public String get(@RequestParam final MultiValueMap<String, String> allParams) throws URISyntaxException {
        // We don't care about the response
        return "SUCCESS";
    }
}