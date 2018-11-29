package nl.wvdzwan.timemachine;

class LibrariesIoApiTest {

    final static String completeProjectJson = "{\"name\":\"junit:junit\",\"platform\":\"Maven\",\"description\":\"\\n    JUnit is a regression testing framework written by Erich Gamma and Kent Beck. It is used by the developer who implements unit tests in Java.\\n  \",\"homepage\":\"http://junit.org\",\"repository_url\":\"http://junit.cvs.sourceforge.net/junit/\",\"normalized_licenses\":[\"CPL-1.0\"],\"rank\":26,\"latest_release_published_at\":\"2014-12-04T16:17:00.000Z\",\"latest_release_number\":\"4.12\",\"language\":\"Java\",\"status\":null,\"package_manager_url\":\"http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22junit%22%20AND%20a%3A%22junit%22\",\"stars\":6855,\"forks\":2661,\"keywords\":[],\"latest_stable_release\":{\"id\":4926598,\"project_id\":416741,\"number\":\"4.8.2\",\"published_at\":\"2010-10-05T08:51:28.000Z\",\"created_at\":\"2016-02-10T07:36:15.796Z\",\"updated_at\":\"2016-02-10T07:36:15.796Z\",\"runtime_dependencies_count\":0},\"latest_download_url\":\"https://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar\",\"dependents_count\":20610,\"dependent_repos_count\":343966,\"versions\":[{\"number\":\"3.7\",\"published_at\":\"2005-08-01T09:42:48.000Z\"},{\"number\":\"3.8\",\"published_at\":\"2005-08-01T09:42:49.000Z\"},{\"number\":\"3.8.1\",\"published_at\":\"2005-08-01T09:42:48.000Z\"},{\"number\":\"3.8.2\",\"published_at\":\"2006-03-03T23:22:26.000Z\"},{\"number\":\"3.8.2-brew\",\"published_at\":\"2008-03-18T21:35:23.000Z\"},{\"number\":\"4.0\",\"published_at\":\"2006-02-16T08:34:50.000Z\"},{\"number\":\"4.1\",\"published_at\":\"2006-05-02T18:54:02.000Z\"},{\"number\":\"4.10\",\"published_at\":\"2011-09-29T19:12:15.000Z\"},{\"number\":\"4.11\",\"published_at\":\"2012-11-14T19:21:20.000Z\"},{\"number\":\"4.11-beta-1\",\"published_at\":\"2012-10-15T19:45:51.000Z\"},{\"number\":\"4.12\",\"published_at\":\"2014-12-04T16:17:00.000Z\"},{\"number\":\"4.12-beta-1\",\"published_at\":\"2014-07-27T20:41:00.000Z\"},{\"number\":\"4.12-beta-2\",\"published_at\":\"2014-09-25T05:54:40.000Z\"},{\"number\":\"4.12-beta-3\",\"published_at\":\"2014-11-09T15:53:00.000Z\"},{\"number\":\"4.2\",\"published_at\":\"2006-11-16T22:25:14.000Z\"},{\"number\":\"4.3\",\"published_at\":\"2007-03-20T15:13:18.000Z\"},{\"number\":\"4.3.1\",\"published_at\":\"2007-03-29T20:46:54.000Z\"},{\"number\":\"4.4\",\"published_at\":\"2007-07-19T14:13:36.000Z\"},{\"number\":\"4.5\",\"published_at\":\"2008-08-08T19:30:10.000Z\"},{\"number\":\"4.6\",\"published_at\":\"2009-05-09T19:38:32.000Z\"},{\"number\":\"4.7\",\"published_at\":\"2009-08-12T15:54:07.000Z\"},{\"number\":\"4.8\",\"published_at\":\"2010-10-05T08:50:56.000Z\"},{\"number\":\"4.8.1\",\"published_at\":\"2010-02-25T12:21:58.000Z\"},{\"number\":\"4.8.2\",\"published_at\":\"2010-10-05T08:51:28.000Z\"},{\"number\":\"4.9\",\"published_at\":\"2011-08-22T18:15:37.000Z\"}]}";


//    @Test
//    void getCorrectRequestUrl() throws IOException {
//
//        HttpClientInterface mockHttpClient = mock(HttpClientInterface.class);
//        LibrariesIoClient api = new LibrariesIoClient("not_an_actual_api_key", mockHttpClient);
//
//
//        api.getProjectInfo("junit:junit");
//
//
//        verify(mockHttpClient).get("https://libraries.io/api/maven/junit:junit?api_key=not_an_actual_api_key");
//    }
//
//    @Test
//    void buildProjectFromJson() throws IOException {
//        HttpClientInterface mockHttpClient = mock(HttpClientInterface.class);
//        LibrariesIoClient api = new LibrariesIoClient("not_an_actual_api_key", mockHttpClient);
//        when(mockHttpClient.get(anyString())).thenReturn(completeProjectJson);
//
//        Project project = null;
//        try {
//            project = api.getProjectInfo("junit:junit");
//        } catch (Exception e) {
//            fail(e);
//        }
//
//        assertEquals("junit:junit", project.getName());
//        assertEquals("Maven", project.getPlatform());
//    }
}