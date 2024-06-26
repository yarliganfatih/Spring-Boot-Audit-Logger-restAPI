package com.draft.restapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/draft")
public class DraftController {

    public static class ReqData {
        private Integer key;
        private String field;

        public ReqData(Integer key, String field) {
            this.key = key;
            this.field = field;
        }

        public Integer getKey() {
            return key;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    private static class ResData {
        private String message;

        public ResData(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @GetMapping
    public ResponseEntity<ResData> index() {
        ResData response = new ResData("Hello World!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/error")
    public ResponseEntity<ResData> error() {
        ResData response = new ResData("Bad Request");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @GetMapping({ "/param", "/query" }) // multi mapping
    public ResponseEntity<ResData> param(@RequestParam Integer id) {
        ResData response = new ResData("param is " + id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/path/{slug}/{id}")
    public ResponseEntity<ResData> path(@PathVariable("slug") String slug, @PathVariable("id") Integer id) {
        ResData response = new ResData(slug + " is " + id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/post")
    public ResponseEntity<ResData> post(@RequestBody ReqData req) {
        ResData response = new ResData("key is " + req.getKey() + ", field is " + req.getField());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
      
    // endpoints specific to roles

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/admin")
    public ResponseEntity<ResData> admin() {
        ResData response = new ResData("Hello admin");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // According to the hierarchy, both admin and mod can access, 
    // but alternative_mod cannot access even though it is at the same level
    @PreAuthorize("hasRole('ROLE_mod')")
    @GetMapping("/mod")
    public ResponseEntity<ResData> mod() {
        ResData response = new ResData("Hello mod");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // This endpoint is only for chosen ones (has no level)
    @PreAuthorize("hasRole('ROLE_chosen')")
    @GetMapping("/chosen")
    public ResponseEntity<ResData> chosen() {
        ResData response = new ResData("Hello chosen one");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
