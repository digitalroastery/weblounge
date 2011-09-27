var ImageMappings = {};

ImageMappings.ImagesCollection = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.ImagesCollection'
});

ImageMappings.ImageType = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.ImageType'
});

ImageMappings.User = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.User'
});

ImageMappings.GPS = new Jsonix.Model.ClassInfo({
	name: 'ImageMappings.GPS'
});

ImageMappings.CreatedModified = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.CreatedModified'
});

ImageMappings.Published = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.Published'
});

ImageMappings.Security = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.Security'
});

ImageMappings.Security.Owner = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.Security.Owner'
});

ImageMappings.Locked = new Jsonix.Model.ClassInfo({
	name: 'ImageMappings.Locked'
});

ImageMappings.Head = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.Head'
});

ImageMappings.Head.Metadata = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.Head.Metadata'
});

ImageMappings.Body = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.Body'
});

ImageMappings.Body.Content = new Jsonix.Model.ClassInfo({
  name: 'ImageMappings.Body.Content'
});

// <user id="my.id" realm="weblounge">My Name</user>
ImageMappings.User.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'id',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'realm',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ValuePropertyInfo({
  name: 'name',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
})];

//<gps lat="47.323323" lng="8.321939" />
ImageMappings.GPS.properties = [new Jsonix.Model.AttributePropertyInfo({
	name: 'lat',
	typeInfo: Jsonix.Schema.XSD.Double.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
	name: 'lng',
	typeInfo: Jsonix.Schema.XSD.Double.INSTANCE
})];

// <created|modified><user .../><date.../></created|modified>
ImageMappings.CreatedModified.properties = [new Jsonix.Model.ElementPropertyInfo({
  name: 'user',
  typeInfo: ImageMappings.User
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'date',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
})];

ImageMappings.Published.properties = [new Jsonix.Model.ElementPropertyInfo({
  name: 'user',
  typeInfo: ImageMappings.User
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'from',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'to',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
})];

ImageMappings.Security.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'owner',
	typeInfo: ImageMappings.Security.Owner
})];

ImageMappings.Security.Owner.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'user',
	typeInfo: ImageMappings.User
})];

ImageMappings.Locked.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'user',
	typeInfo: ImageMappings.User
})]

ImageMappings.ImagesCollection.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'image',
	collection: true,
	typeInfo: ImageMappings.ImageType
})];

ImageMappings.ImageType.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'id',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'path',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'version',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'head',
  typeInfo: ImageMappings.Head
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'body',
  typeInfo: ImageMappings.Body
})];

ImageMappings.Head.properties = [new Jsonix.Model.ElementPropertyInfo({
  name: 'template',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'layout',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'promote',
  typeInfo: Jsonix.Schema.XSD.Boolean.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'index',
  typeInfo: Jsonix.Schema.XSD.Boolean.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'metadata',
  typeInfo: ImageMappings.Head.Metadata
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'created',
  typeInfo: ImageMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'modified',
  typeInfo: ImageMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'published',
  typeInfo: ImageMappings.Published
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'security',
	typeInfo: ImageMappings.Security
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'locked',
	typeInfo: ImageMappings.Locked
})];

ImageMappings.Head.Metadata.properties = [new Jsonix.Model.ElementMapPropertyInfo({
	name: "title",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'subject',
  collection: true,
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: "description",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'type',
	typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: "coverage",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: "rights",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
})];

// <body>...</body>
ImageMappings.Body.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'contents',
	collection: true,
	elementName: new Jsonix.XML.QName('content'),
	typeInfo: ImageMappings.Body.Content
})];

// <composer id="composer-id">...</composer>
ImageMappings.Body.Content.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'language',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'created',
  typeInfo: ImageMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'filename',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'mimetype',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'size',
  typeInfo: Jsonix.Schema.XSD.Integer.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'width',
  typeInfo: Jsonix.Schema.XSD.Integer.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'height',
  typeInfo: Jsonix.Schema.XSD.Integer.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'photographer',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'datetaken',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'location',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'gps',
  typeInfo: FileMappings.GPS
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'filmspeed',
  typeInfo: Jsonix.Schema.XSD.Integer.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'fnumber',
  typeInfo: Jsonix.Schema.XSD.Float.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'focalwidth',
  typeInfo: Jsonix.Schema.XSD.Integer.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'exposuretime',
  typeInfo: Jsonix.Schema.XSD.Float.INSTANCE
})];

ImageMappings.typeInfos = [ImageMappings.Head];
ImageMappings.elementInfos = [{
  elementName: new Jsonix.XML.QName('images'),
  typeInfo: ImageMappings.ImagesCollection
},
{
  elementName: new Jsonix.XML.QName('image'),
  typeInfo: ImageMappings.ImageType
},
{
  elementName: new Jsonix.XML.QName('head'),
  typeInfo: ImageMappings.Head
},
{
  elementName: new Jsonix.XML.QName('content'),
  typeInfo: ImageMappings.Body.Content
}];