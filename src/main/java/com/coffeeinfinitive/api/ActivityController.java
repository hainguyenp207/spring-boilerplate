package com.coffeeinfinitive.api;

import com.coffeeinfinitive.Utils;
import com.coffeeinfinitive.constants.ResultCode;
import com.coffeeinfinitive.dao.entity.Activity;
import com.coffeeinfinitive.dao.entity.Comment;
import com.coffeeinfinitive.dao.entity.Register;
import com.coffeeinfinitive.dao.entity.User;
import com.coffeeinfinitive.exception.CoffeeSystemErrorException;
import com.coffeeinfinitive.model.ActivityForm;
import com.coffeeinfinitive.model.UserForm;
import com.coffeeinfinitive.service.*;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Created by jinz on 4/15/17.
 * Controller quản lý toàn bộ hoạt động
 */
@RestController
@RequestMapping(path = "/api/v1/activities")
public class ActivityController {

    @Autowired
    ActivityService activityService;
    @Autowired
    UserService userService;
    @Autowired
    RegisterService registerService;
    @Autowired
    ValidatorService validatorService;
    @Autowired
    CommentService commentService;

    @GetMapping
    public ResponseEntity<List<Activity>> getActivities() {
        List<Activity> activities = activityService.getActivities();
        return new ResponseEntity<List<Activity>>(activities, HttpStatus.OK);
    }

    /*
    Lay danh sach dang ky theo hoat dong
     */
    @GetMapping(path = "/{id}/registers")
    public ResponseEntity<?> getRegisters(@PathVariable("id") String id) {
        Activity activity = activityService.findActivityById(id);
        if (activity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Set<Register> registers = activity.getRegisters();
        return new ResponseEntity<>(registers, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}/registers/{idRegister}")
    public ResponseEntity<?> getRegister(@PathVariable("id") String id,
                                         @PathVariable("idRegister") String idRegister) {
        Activity activity = activityService.findActivityById(id);

        Register register = registerService.findRegister(idRegister);
        if (register == null) {
            JsonObject result = new JsonObject();
            result.addProperty("code", ResultCode.REGISTER_NOT_FOUND.getCode());
            result.addProperty("message", ResultCode.REGISTER_NOT_FOUND.getMessageVn());
            return new ResponseEntity<>(result.toString(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(register, HttpStatus.OK);
    }

    /*
    Lay comment cua 1 hoat dong
     */
    @GetMapping(path = "/{id}/comments")
    public ResponseEntity<?> getComment(@PathVariable("id") String id) {
        Activity activity = activityService.findActivityById(id);

        if (activity == null) {
            JsonObject result = new JsonObject();
            result.addProperty("code", ResultCode.ACTIVITY_NOT_FOUND.getCode());
            result.addProperty("message", ResultCode.ACTIVITY_NOT_FOUND.getMessageVn());
            return new ResponseEntity<>(result.toString(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(activity.getComments(), HttpStatus.OK);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Activity> getActivity(@PathVariable("id") String id) {
        Activity activity = activityService.findActivityById(id);
        if (activity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(activity, HttpStatus.OK);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addActivity(Authentication auth, @RequestBody ActivityForm activityForm) {
        Utils<ActivityForm, Activity> convert = new Utils<>(Activity.class);
        Activity currentActivity = activityService.findActivityById(activityForm.getId());
        ResponseEntity responseEntity = validatorService.checkExistActivity(currentActivity);
        if (responseEntity.getStatusCode().value() == 409) {
            return responseEntity;
        }

        User createdBy = userService.findUserById(auth.getPrincipal().toString());
        Activity newActivity = convert.ConvertObject(activityForm);
        // Init data;
        newActivity.setCreatedDate();
        newActivity.setLastUpdatedDate();
        newActivity.setCreatedBy(createdBy);
        newActivity.setLastUpdatedBy(createdBy);
        Activity savedActivity = activityService.save(newActivity);
        return new ResponseEntity<Activity>(savedActivity, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> updateActivity(Authentication auth, @PathVariable("id") String id, @RequestBody ActivityForm activityForm) {
        Activity currentActivity = activityService.findActivityById(id);
        if (currentActivity == null) {
            JsonObject result = new JsonObject();
            result.addProperty("code", ResultCode.ACTIVITY_NOT_FOUND.getCode());
            result.addProperty("message", ResultCode.ACTIVITY_NOT_FOUND.getMessageVn());
            return new ResponseEntity<>(result.toString(), HttpStatus.NOT_FOUND);
        }
        Utils<ActivityForm, Activity> convert = new Utils<>(Activity.class);
        User updatedBy = userService.findUserById(auth.getPrincipal().toString());
        Activity newActivity = convert.ConvertObject(activityForm);
        // Init data;
        currentActivity = newActivity;
        currentActivity.setLastUpdatedDate();
        currentActivity.setLastUpdatedBy(updatedBy);
        try {
            Activity savedActivity = activityService.update(currentActivity);
            return new ResponseEntity<Activity>(savedActivity, HttpStatus.OK);
        } catch (Exception e) {
            throw new CoffeeSystemErrorException("", e);
        }
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> deleteActivity(Authentication auth, @PathVariable("id") String id) {
        Activity currentActivity = activityService.findActivityById(id);
        if (currentActivity == null) {
            JsonObject result = new JsonObject();
            result.addProperty("code", ResultCode.ACTIVITY_NOT_FOUND.getCode());
            result.addProperty("message", ResultCode.ACTIVITY_NOT_FOUND.getMessageVn());
            return new ResponseEntity<>(result.toString(), HttpStatus.NOT_FOUND);
        }

        try {
            activityService.delete(id);
            return new ResponseEntity<Activity>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new CoffeeSystemErrorException("", e);
        }
    }

}