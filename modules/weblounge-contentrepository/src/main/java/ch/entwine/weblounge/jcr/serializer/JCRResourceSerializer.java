package ch.entwine.weblounge.jcr.serializer;

import ch.entwine.weblounge.common.content.Resource;

import javax.jcr.Node;

public interface JCRResourceSerializer {

  String[] getSerializableTypes();

  void store(Node node, Resource<?> resource);

  void read(Node node, Resource<?> resource);

}