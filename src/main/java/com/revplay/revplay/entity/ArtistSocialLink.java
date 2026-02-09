package com.revplay.revplay.entity;

import com.revplay.revplay.enums.SocialPlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "artist_social_links",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_artist_platform",
                columnNames = {"artist_id", "platform"}
        )
)
public class ArtistSocialLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 30)
    private SocialPlatform platform;

    @Column(name = "url", nullable = false, length = 500)
    private String url;
}
