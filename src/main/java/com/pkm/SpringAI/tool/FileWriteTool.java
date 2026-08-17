package com.pkm.SpringAI.tool;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
@Slf4j
public class FileWriteTool implements AgenticTool {

    @Value("${app.tools.file-output-dir:./agent-output}")
    private String outputDir;

    @Tool(description = "Write text content to a file. Provide a filename (e.g. 'report.txt') and the content to write.")
    public String writeToFile(
            @ToolParam(description = "Target filename, e.g. 'report.txt'") String filename,
            @ToolParam(description = "Full text content to write to the file") String content) {
        log.info("Write text content to file: {}", filename);
        try {
            Path dir = Path.of(outputDir);
            Files.createDirectories(dir);

            // sanitize filename to prevent path traversal
            String safeName = Path.of(filename).getFileName().toString();
            Path filePath = dir.resolve(safeName);

            Files.writeString(filePath, content,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return "Successfully wrote " + content.length() + " characters to " + filePath.toAbsolutePath();
        } catch (IOException e) {
            return "Failed to write file: " + e.getMessage();
        }
    }

    @Tool(description = "Append text content to an existing file, or create it if it doesn't exist.")
    public String appendToFile(
            @ToolParam(description = "Target filename, e.g. 'report.txt'") String filename,
            @ToolParam(description = "Text content to append to the file") String content) {
        try {
            Path dir = Path.of(outputDir);
            Files.createDirectories(dir);
            String safeName = Path.of(filename).getFileName().toString();
            Path filePath = dir.resolve(safeName);

            Files.writeString(filePath, content + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            return "Appended content to " + filePath.toAbsolutePath();
        } catch (IOException e) {
            return "Failed to append to file: " + e.getMessage();
        }
    }
}