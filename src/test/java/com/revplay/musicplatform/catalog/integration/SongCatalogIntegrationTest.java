package com.revplay.musicplatform.catalog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.artist.entity.Artist;
import com.revplay.musicplatform.artist.enums.ArtistType;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.catalog.dto.request.SongCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.SongUpdateRequest;
import com.revplay.musicplatform.catalog.entity.Song;
import com.revplay.musicplatform.catalog.repository.SongRepository;
import com.revplay.musicplatform.common.MockSecurityContextHelper;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockSecurityContextHelper.class)
@Transactional
class SongCatalogIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private SongRepository songRepository;

        @Autowired
        private ArtistRepository artistRepository;

        @Autowired
        private MockSecurityContextHelper securityContextHelper;

        private Long artistUserId = 100L;
        private Long artistId;

        @BeforeEach
        void setUp() {
                // Create an artist for testing
                Artist artist = new Artist();
                artist.setUserId(artistUserId);
                artist.setDisplayName("Test Artist");
                artist.setArtistType(ArtistType.MUSIC);
                artist.setVerified(true);
                artistRepository.save(artist);
        }

        @Test
        @DisplayName("End-to-end: upload song -> update -> delete")
        void songLifecycle_IntegrationTest() throws Exception {
                // 1. Upload Song
                securityContextHelper.setMockUser(artistUserId, "artistUser", UserRole.ARTIST.name());

                SongCreateRequest createRequest = new SongCreateRequest();
                createRequest.setTitle("Integration Song");
                // provide duration since mock file won't have metadata
                createRequest.setDurationSeconds(180);

                String metadata = objectMapper.writeValueAsString(createRequest);
                MockMultipartFile filePart = new MockMultipartFile("file", "test.mp3", "audio/mpeg",
                                "fake audio content".getBytes());
                MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json",
                                metadata.getBytes());

                String responseJson = mockMvc.perform(multipart("/api/v1/songs")
                                .file(filePart)
                                .file(metadataPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("Integration Song"))
                                .andReturn().getResponse().getContentAsString();

                Long songId = objectMapper.readTree(responseJson).get("data").get("songId").asLong();

                // 2. Update Song
                SongUpdateRequest updateRequest = new SongUpdateRequest();
                updateRequest.setTitle("Updated Integration Song");
                updateRequest.setDurationSeconds(200);

                mockMvc.perform(put("/api/v1/songs/" + songId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title").value("Updated Integration Song"));

                // 3. Verify in DB
                Optional<Song> song = songRepository.findById(songId);
                assertThat(song).isPresent();
                assertThat(song.get().getTitle()).isEqualTo("Updated Integration Song");

                // 4. Delete Song
                mockMvc.perform(delete("/api/v1/songs/" + songId))
                                .andExpect(status().isOk());

                // 5. Verify soft delete
                Optional<Song> deletedSong = songRepository.findById(songId);
                assertThat(deletedSong).isPresent();
                assertThat(deletedSong.get().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("RBAC: LISTENER upload song returns 401 in current implementation")
        void listener_CannotUploadSong() throws Exception {
                securityContextHelper.setMockUser(1L, "listenerUser", UserRole.LISTENER.name());

                SongCreateRequest createRequest = new SongCreateRequest();
                createRequest.setTitle("Illegal Song");
                createRequest.setDurationSeconds(120);

                String metadata = objectMapper.writeValueAsString(createRequest);
                MockMultipartFile filePart = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "data".getBytes());
                MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json",
                                metadata.getBytes());

                mockMvc.perform(multipart("/api/v1/songs")
                                .file(filePart)
                                .file(metadataPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isUnauthorized());
        }
}

