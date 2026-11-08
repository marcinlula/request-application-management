package com.marcinlula.requestmanagement.api;

import com.marcinlula.requestmanagement.RequestService;
import com.marcinlula.requestmanagement.exception.InvalidRequestDataException;
import com.marcinlula.requestmanagement.exception.InvalidRequestStateException;
import com.marcinlula.requestmanagement.exception.RequestNotFoundException;
import com.marcinlula.requestmanagement.model.Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.marcinlula.requestmanagement.model.RequestState.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@Import(RequestApiMapperImpl.class)
class RequestControllerTest {

    private static final String URL = "/api/v1/application-requests";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RequestService service;

    @Test
    void whenCreateWithValidBody_thenCreatedIsReturnedWithRequest() throws Exception {
        //given
        Request request = Request.create("name", "body");
        when(service.create("name", "body")).thenReturn(request);

        //when
        var result = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"name","body":"body"}
                        """));

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(request.id().toString()))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.state").value("CREATED"));
    }

    @Test
    void whenCreateWithBlankName_thenBadRequestIsReturnedAndServiceIsNotCalled() throws Exception {
        //given
        //when
        var result = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"body\":\"body\"}"));

        //then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void whenCreateWithMissingBody_thenBadRequestIsReturned() throws Exception {
        //given
        //when
        var result = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"name\"}"));

        //then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void whenGetExisting_thenRequestIsReturned() throws Exception {
        //given
        Request request = Request.create("name", "body");
        when(service.get(request.id())).thenReturn(request);

        //when
        var result = mockMvc.perform(get(URL + "/" + request.id()));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.id().toString()))
                .andExpect(jsonPath("$.body").value("body"));
    }

    @Test
    void whenGetHistory_thenStateChangesAreReturned() throws Exception {
        //given
        Request request = Request.create("name", "body").changeState(REJECTED, "bad");
        when(service.getHistory(request.id())).thenReturn(request.history());

        //when
        var result = mockMvc.perform(get(URL + "/" + request.id() + "/history"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].from").doesNotExist())
                .andExpect(jsonPath("$[0].to").value("CREATED"))
                .andExpect(jsonPath("$[1].from").value("CREATED"))
                .andExpect(jsonPath("$[1].to").value("REJECTED"))
                .andExpect(jsonPath("$[1].reason").value("bad"));
    }

    @Test
    void whenGetHistoryOfUnknownRequest_thenNotFoundIsReturned() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        when(service.getHistory(id)).thenThrow(new RequestNotFoundException(id));

        //when
        var result = mockMvc.perform(get(URL + "/" + id + "/history"));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    void whenGetUnknown_thenNotFoundIsReturned() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new RequestNotFoundException(id));

        //when
        var result = mockMvc.perform(get(URL + "/" + id));

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Request " + id + " not found"));
    }

    @Test
    void whenGetWithMalformedId_thenBadRequestIsReturned() throws Exception {
        //given
        //when
        var result = mockMvc.perform(get(URL + "/not-a-uuid"));

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void whenChangeBody_thenUpdatedRequestIsReturned() throws Exception {
        //given
        Request request = Request.create("name", "body").withBody("new");
        when(service.changeBody(request.id(), "new")).thenReturn(request);

        //when
        var result = mockMvc.perform(patch(URL + "/" + request.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"body\":\"new\"}"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("new"));
    }

    @Test
    void whenChangeBodyToBlank_thenBadRequestIsReturned() throws Exception {
        //given
        //when
        var result = mockMvc.perform(patch(URL + "/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"body\":\"\"}"));

        //then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void whenVerify_thenVerifiedRequestIsReturned() throws Exception {
        //given
        Request request = Request.create("name", "body").withState(VERIFIED);
        when(service.verify(request.id())).thenReturn(request);

        //when
        var result = mockMvc.perform(post(URL + "/" + request.id() + "/verify"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("VERIFIED"));
    }

    @Test
    void whenAccept_thenAcceptedRequestIsReturned() throws Exception {
        //given
        Request request = Request.create("name", "body").withState(ACCEPTED);
        when(service.accept(request.id())).thenReturn(request);

        //when
        var result = mockMvc.perform(post(URL + "/" + request.id() + "/accept"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));
    }

    @Test
    void whenPublish_thenPublicationNumberIsReturned() throws Exception {
        //given
        Request request = Request.create("name", "body").withState(PUBLISHED).withPublicationNumber(5L);
        when(service.publish(request.id())).thenReturn(request);

        //when
        var result = mockMvc.perform(post(URL + "/" + request.id() + "/publish"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$.publicationNumber").value(5));
    }

    @Test
    void whenTransitionNotAllowed_thenConflictIsReturned() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        when(service.publish(id)).thenThrow(new InvalidRequestStateException("Transition from CREATED to PUBLISHED is not allowed"));

        //when
        var result = mockMvc.perform(post(URL + "/" + id + "/publish"));

        //then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Transition from CREATED to PUBLISHED is not allowed"));
    }

    @Test
    void whenReject_thenRejectedRequestWithReasonIsReturned() throws Exception {
        //given
        Request request = Request.create("name", "body").withState(REJECTED).withReason("bad");
        when(service.reject(request.id(), "bad")).thenReturn(request);

        //when
        var result = mockMvc.perform(post(URL + "/" + request.id() + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"bad\"}"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REJECTED"))
                .andExpect(jsonPath("$.reason").value("bad"));
    }

    @Test
    void whenDelete_thenDeletedRequestWithReasonIsReturned() throws Exception {
        //given
        Request request = Request.create("name", "body").withState(DELETED).withReason("duplicate");
        when(service.delete(request.id(), "duplicate")).thenReturn(request);

        //when
        var result = mockMvc.perform(post(URL + "/" + request.id() + "/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"duplicate\"}"));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DELETED"))
                .andExpect(jsonPath("$.reason").value("duplicate"));
    }

    @Test
    void whenRejectOrDeleteWithoutReason_thenBadRequestIsReturnedAndServiceIsNotCalled() throws Exception {
        //given
        UUID id = UUID.randomUUID();

        //when
        var reject = mockMvc.perform(post(URL + "/" + id + "/reject")
                .contentType(MediaType.APPLICATION_JSON).content("{}"));
        var delete = mockMvc.perform(post(URL + "/" + id + "/delete")
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\" \"}"));

        //then
        reject.andExpect(status().isBadRequest());
        delete.andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void whenServiceRejectsData_thenBadRequestIsReturned() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        when(service.changeBody(any(), any())).thenThrow(new InvalidRequestDataException("Body is required"));

        //when
        var result = mockMvc.perform(patch(URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"body\":\"x\"}"));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Body is required"));
    }
}
