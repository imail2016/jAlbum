package com.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.backend.FileInfo;
import com.backend.UniqPhotosStore;
import com.utils.web.GenerateHTML;

public class ObjectService
{
    private static final Logger logger = LoggerFactory.getLogger(ObjectService.class);

    private String id;

    public ObjectService(String id)
    {
        this.setId(id);
    }

    @GET
    public Response getPhotoData(@Context HttpServletRequest req, @Context HttpServletResponse response)
            throws IOException
    {
        ResponseBuilder builder = Response.status(200);
        UniqPhotosStore meta = UniqPhotosStore.getInstance();
        FileInfo f = meta.getOneFileByHashStr(id);
        if ("true".equalsIgnoreCase(req.getParameter("content")))
        {
            if (f != null)
            {
                BufferedInputStream fi = new BufferedInputStream(new FileInputStream(new File(f.getPath())));
                builder.entity(fi);
                String contenttype = getContentType(f.getPath());
                builder.header("Content-type", contenttype);
                long expirAge = 3600 * 1000 * 24 * 7;
                long expirtime = System.currentTimeMillis() + expirAge;
                builder.header("Expires", new Date(expirtime));
                builder.header("Cache-Control", "max-age=" + expirAge);
                logger.info("the file is: {}, Mime: {}", f, contenttype);
            }
            else
            {
                builder.status(404);
            }
        }
        else
        {
            String bodyContent = GenerateHTML.generateSinglePhoto(f);
            builder.header("Content-type", "text/html");
            builder.entity(bodyContent);
            logger.info("the page is {}", bodyContent);
        }

        return builder.build();
    }

    private static String getContentType(String pathToFile) throws IOException
    {
        return Files.probeContentType(Paths.get(pathToFile));
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
