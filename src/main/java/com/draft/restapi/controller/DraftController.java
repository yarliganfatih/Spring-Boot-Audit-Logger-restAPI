package com.draft.restapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RestController
@Validated
@RequestMapping("/api/draft")
public class DraftController {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ReqData {
        @Min(1)
        private Integer key;
        
        @Size(min = 3, max = 100)
        private String field;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class ResData {
        private String message;
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
    public ResponseEntity<ResData> param(@NotNull @RequestParam Integer id) {
        ResData response = new ResData("param is " + id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/path/{slug}/{id}")
    public ResponseEntity<ResData> path(@Size(min = 3, max = 100) @PathVariable("slug") String slug, @Min(1) @PathVariable("id") Integer id) {
        ResData response = new ResData(slug + " is " + id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/form")
    public ResponseEntity<ResData> form(@Valid @ModelAttribute ReqData req) {
        ResData response = new ResData("key is " + req.getKey() + ", field is " + req.getField());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResData> upload(@RequestParam("file") MultipartFile file, @RequestParam("description") String description) {
        ResData response = new ResData("File " + file.getOriginalFilename() + " uploaded with description: " + description);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/post")
    public ResponseEntity<ResData> post(@Valid @RequestBody ReqData req) {
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
