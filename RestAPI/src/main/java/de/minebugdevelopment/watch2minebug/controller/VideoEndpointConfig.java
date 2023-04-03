package de.minebugdevelopment.watch2minebug.controller;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoEndpointConfig {
/*    @Autowired
    private VideoStreamService service;

    @Bean
    public RouterFunction<ServerResponse> router(){
        return RouterFunctions.route()
                .GET("video/{name}", this::videoHandler)
                .build();
    }

    private Mono<ServerResponse> videoHandler(ServerRequest serverRequest){
        String title = serverRequest.pathVariable("name");
        return ServerResponse.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(this.service.getVideo(title), Resource.class);
    }*/

}