package org.example.autopark.exportAndImport.ref.entry.web;

import jakarta.servlet.http.HttpServletResponse;
import org.example.autopark.exportAndImport.ref.format.DataFormat;
import org.springframework.stereotype.Component;

@Component
public class DownloadResponseConfigurer {
    public void prepare(HttpServletResponse response, String baseFilename, DataFormat format) {
        response.setContentType(format.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=" + baseFilename + "." + format.getExtension());
    }
}
