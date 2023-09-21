package com.jenkins_project.jenkins_project.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jenkins_project.jenkins_project.service.AwsStorageService;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class AwsStorageController {

    private final AwsStorageService awsStorageService;


    /**
     * Method to get all files saved into the Amazon S3 storage
     *
     * @return List<String> fileNames
     */
    @GetMapping("/all")
    private ResponseEntity<?> fileNames() {
        List<String> fileNames = awsStorageService.getFileNames();

        return new ResponseEntity<>(fileNames, HttpStatus.OK);
    }

    @PostMapping("/all")
    private ResponseEntity<?> upload(MultipartFile file) throws IOException {

        long startTime = System.currentTimeMillis();

        String key = file.getOriginalFilename();

        awsStorageService.uploadFiles(file, key);
        awsStorageService.sendMessage(file);


        long endTime = System.currentTimeMillis();

        long elapsedTime = endTime - startTime;
        long seconds = (elapsedTime/100)%60;

        String time = "Time elapsed was: " + elapsedTime + " milisecond, or 0." + seconds + " seconds";

        return new ResponseEntity<>(time, HttpStatus.OK);
    }

    @PostMapping("sendMessage")
    public void sendMessage(@RequestParam("text") String text) {
    }
}
