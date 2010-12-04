Create two directories "import" and "export" anywhere in your filesystem.

1. Extract the weblounge 2.0 XML dump to the <import> directory, so that it
looks like <import>/db/weblounge etc.

2. TODO: <Repository files> ?

3. Run the importer:

   java -jar weblounge-importer.jar -i <import> -o <export> -s <site>
   
4. Move <export>/<site> to your Weblounge 3 installation.