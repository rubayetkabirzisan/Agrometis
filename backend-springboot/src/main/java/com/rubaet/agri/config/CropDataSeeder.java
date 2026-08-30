package com.rubaet.agri.config;

import com.rubaet.agri.entity.Crop;
import com.rubaet.agri.repository.CropRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class CropDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CropDataSeeder.class);

    private final CropRepository cropRepository;

    public CropDataSeeder(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (cropRepository.count() > 0) {
            log.info("Crops already seeded — skipping.");
            return;
        }

        log.info("Seeding crops from text files...");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            seedFromPattern(resolver, "classpath:data/CSVFILES/ZishanFiles/ZishanCSVFILES/*.txt", "All Seasons", false);
            seedFromPattern(resolver, "classpath:data/CSVFILES/ZishanFiles/Summary info/*.txt", "Summary", true);
            log.info("Crop data successfully seeded.");
        } catch (Exception e) {
            log.error("Failed to read crop data resources", e);
        }
    }

    private void seedFromPattern(PathMatchingResourcePatternResolver resolver,
                                  String pattern, String season, boolean isSummary) throws Exception {
        Resource[] resources = resolver.getResources(pattern);
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) continue;

            String name = filename.replace(".txt", "").trim();
            if (isSummary) {
                name = name.replace("summary", "").replace("Summary", "").trim() + " Summary";
            }

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                String content = FileCopyUtils.copyToString(reader);
                Crop crop = new Crop();
                crop.setName(name);
                crop.setSeason(season);
                crop.setDescription(content.trim());
                try {
                    cropRepository.save(crop);
                } catch (Exception e) {
                    log.debug("Skipped duplicate crop: {}", name);
                }
            }
        }
    }
}
