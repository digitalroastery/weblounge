Preparation
-----------
1. Extract the desired site out of the Weblounge 2.0 XML dump. You only need the site folder 
under <xmldump>/db/weblounge/sites.

2. Get a copy of the Weblounge 2.0 repository and merge it with the XML repository information.
cp -r <repository>/* <mysite>/repository/

3. Run the importer:

   java -jar weblounge-importer-3.0-SNAPSHOT-jar-with-dependencies.jar -i /home/db/weblounge/sites/<mysite> -o /targe/directory
   
4. Move <export> to your Weblounge 3 installation.