package no.nsb.esbenmock.controller;

/**
 * Created by ovalle on 30/10/15.
 */

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
public class EsbenController {

    final Logger logger = LogManager.getLogger(EsbenController.class);
    final Logger searchLog = LogManager.getLogger("search-log");

    private String server = "esbentest.knowit.no";
    private int port = 8097;

//    private RestTemplate restTemplate;

//    @Autowired
//    public EsbenController(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }

//    @Autowired
    RestTemplate restTemplate = new RestTemplate();

    @Value("http://esbentest.knowit.no:8097")
    String targetBaseUrl;


    @RequestMapping(value = "/itineraryservices5/{operation}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"})
    public @ResponseBody
    String doIternaryRequest(
            @PathVariable("operation") String operation,
            @RequestParam MultiValueMap<String, String> allRequestParams,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info(getFullURL(request));

        try {
            ResponseErrorHandler errorHandler = new ResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                    HttpStatus status = clientHttpResponse.getStatusCode();
                    HttpStatus.Series series = status.series();
                    return (HttpStatus.Series.CLIENT_ERROR.equals(series)
                            || HttpStatus.Series.SERVER_ERROR.equals(series));
                }

                @Override
                public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
                    String errorText = IOUtils.toString(clientHttpResponse.getBody(), "UTF-8");
                    throw new HttpClientErrorException(clientHttpResponse.getStatusCode(), errorText);
                }
            };

            String subpath = "/data/", encoding = "UTF-8";

            String resource = IOUtils.toString(this.getClass().getResourceAsStream(subpath + operation + ".json"), encoding);

            return resource;

        } catch (HttpClientErrorException hcee) {
            logger.error(hcee.getStatusCode().value() + " " + hcee.getStatusText());

        } catch (Exception e) {
        }
        return null;
    }


    @RequestMapping(value = "/**", method = RequestMethod.GET)
    @ResponseBody
    public String mirrorRestPost(HttpMethod method, HttpServletRequest request,
                                 HttpServletResponse response) throws URISyntaxException
    {



        URI uri = new URI("http", null, server, port, request.getRequestURI(), request.getQueryString(), null);

        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, method, new HttpEntity<>(""), String.class);

        return responseEntity.getBody();
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    @ResponseBody
    public String mirrorRestGet(@RequestBody String body, HttpMethod method, HttpServletRequest request,
                                 HttpServletResponse response) throws URISyntaxException
    {
        URI uri = new URI("http", null, server, port, request.getRequestURI(), request.getQueryString(), null);

        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, method, new HttpEntity<>(body), String.class);

        return responseEntity.getBody();
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }
}
