package consulo.app.plugins.frontend.sitemap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author VISTALL
 * @since 2026-02-18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonLd {
    public static class Offer {
        @JsonProperty(value = "@type", index = 0)
        public String type = "Offer";

        public String price = "0";

        public String priceCurrency = "USD";
    }

    @JsonProperty(value = "@context", index = 0)
    public String context = "https://schema.org";

    @JsonProperty(value = "@type", index = 1)
    public String type = "SoftwareApplication";

    public String name;

    public String headline;

    public String description;

    public String url;

    public String keywords;

    public String downloadUrl;

    public String softwareVersion;
    
    public String dateModified;

    public String image;

    public String screenshot;

    //public String dateModified = "Jun 27, 2020";

    public String applicationCategory = "DeveloperApplication";

    public String applicationSubCategory = "IDE Plugin";

    public String operatingSystem = "Windows, macOS, Linux";

    public Offer offers = new Offer();

    /*

       "author":{
      "@type":"Organization",
      "name":"82598864-f191-496f-b034-8ac43414d899",
      "url":"https://plugins.jetbrains.com/vendor/82598864-f191-496f-b034-8ac43414d899"
   },
    "comment":[
  {
     "@type":"Comment",
     "text":"<p>Just to clarify things for those complaining that the plugin is not compatible with the latest IDE versions.  It seems that the author of this plugin, Jon S Akhtar (aka Sylvanaar), unfortunately has passed away: <a href=\"https://twitter.com/Wowhead/status/1397943365291884548\">https://twitter.com/Wowhead/status/1397943365291884548</a></p>\n",
     "datePublished":"Oct 04, 2021",
     "author":{
        "@type":"Person",
        "name":"Martin Thorsen Ranang",
        "url":"https://plugins.jetbrains.com/author/8d88ff8b-c423-40b9-bbe7-85c662236fa9"
     }
  },
  {
     "@type":"Comment",
     "text":"<p>Good, but not updated for the new version of IDE.</p>\n",
     "datePublished":"May 24, 2021",
     "author":{
        "@type":"Person",
        "name":"David Rychlý",
        "url":"https://plugins.jetbrains.com/author/b71087fe-60b8-418f-a553-2233727f4df6"
     }
  }
   ],
   "aggregateRating":{
      "@type":"AggregateRating",
      "ratingValue":4,
      "reviewCount":29
   },
    */
}
