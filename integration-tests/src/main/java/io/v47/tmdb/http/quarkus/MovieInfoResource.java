/*
 * The Clear BSD License
 *
 * Copyright (c) 2022, the tmdb-api-client authors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the disclaimer
 * below) provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *      * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      * Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from this
 *      software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY
 * THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package io.v47.tmdb.http.quarkus;

import com.neovisionaries.i18n.LocaleCode;
import io.smallrye.mutiny.Uni;
import io.v47.tmdb.TmdbClient;
import io.v47.tmdb.api.MovieRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Objects;

@Path("/movie")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MovieInfoResource {
    private final TmdbClient tmdbClient;

    @Inject
    public MovieInfoResource(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    @GET
    @Path("{id}")
    public Uni<MovieInfo> getMovieInfo(@PathParam("id") int id) {
        return Uni.createFrom()
                  .publisher(tmdbClient.getMovie()
                                       .details(id,
                                                LocaleCode.de_DE,
                                                MovieRequest.Changes,
                                                MovieRequest.Credits,
                                                MovieRequest.Lists,
                                                MovieRequest.ReleaseDates))
                  .map(movieDetails -> new MovieInfo(movieDetails.getImdbId(),
                                                     movieDetails.getTitle(),
                                                     Objects.requireNonNull(movieDetails.getReleaseDate()).getYear()));
    }

    @SuppressWarnings("ClassCanBeRecord") // change back once Jackson 2.18.0 has been released
    public static final class MovieInfo {
        private final String imdbId;
        private final String title;
        private final int releaseYear;

        public MovieInfo(String imdbId, String title, int releaseYear) {
            this.imdbId = imdbId;
            this.title = title;
            this.releaseYear = releaseYear;
        }

        public String getImdbId() {
            return imdbId;
        }

        public String getTitle() {
            return title;
        }

        public int getReleaseYear() {
            return releaseYear;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            var that = (MovieInfo) obj;
            return Objects.equals(this.imdbId, that.imdbId) && Objects.equals(this.title,
                                                                              that.title) && this.releaseYear == that.releaseYear;
        }

        @Override
        public int hashCode() {
            return Objects.hash(imdbId, title, releaseYear);
        }

        @Override
        public String toString() {
            return "MovieInfo[" + "imdbId=" + imdbId + ", " + "title=" + title + ", " + "releaseYear=" + releaseYear + ']';
        }
    }
}
