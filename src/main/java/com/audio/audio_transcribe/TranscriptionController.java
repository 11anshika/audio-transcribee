package com.audio.audio_transcribe;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/transcribe")
public class TranscriptionController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    // ✅ Let Spring inject the bean
    public TranscriptionController(OpenAiAudioTranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    @PostMapping
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("audio", ".wav");
        try {
            file.transferTo(tempFile);

            OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                    .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                    .language("en")
                    .temperature(0f)
                    .build();

            FileSystemResource audioFile = new FileSystemResource(tempFile);
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
            AudioTranscriptionResponse response = transcriptionModel.call(prompt);

            return new ResponseEntity<>(response.getResult().getOutput(), HttpStatus.OK);
        } finally {
            tempFile.delete(); // cleanup
        }
    }
}
