var FileMappings = {};

FileMappings.FilesCollection = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.FilesCollection'
});

FileMappings.FileType = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.FileType'
});

FileMappings.User = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.User'
});

FileMappings.CreatedModified = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.CreatedModified'
});

FileMappings.Published = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.Published'
});

FileMappings.Security = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.Security'
});

FileMappings.Security.Owner = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.Security.Owner'
});

FileMappings.Locked = new Jsonix.Model.ClassInfo({
	name: 'FileMappings.Locked'
});

FileMappings.Head = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.Head'
});

FileMappings.Head.Metadata = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.Head.Metadata'
});

FileMappings.Body = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.Body'
});

FileMappings.Body.Content = new Jsonix.Model.ClassInfo({
  name: 'FileMappings.Body.Content'
});

// <user id="my.id" realm="weblounge">My Name</user>
FileMappings.User.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'id',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'realm',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ValuePropertyInfo({
  name: 'name',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
})];

// <created|modified><user .../><date.../></created|modified>
FileMappings.CreatedModified.properties = [new Jsonix.Model.ElementPropertyInfo({
  name: 'user',
  typeInfo: FileMappings.User
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'date',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
})];

FileMappings.Published.properties = [new Jsonix.Model.ElementPropertyInfo({
  name: 'user',
  typeInfo: FileMappings.User
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'from',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'to',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
})];

FileMappings.Security.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'owner',
	typeInfo: FileMappings.Security.Owner
})];

FileMappings.Security.Owner.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'user',
	typeInfo: FileMappings.User
})];

FileMappings.Locked.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'user',
	typeInfo: FileMappings.User
})]

FileMappings.FilesCollection.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'file',
	collection: true,
	typeInfo: FileMappings.FileType
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'image',
	collection: true,
	typeInfo: FileMappings.FileType
})];

FileMappings.FileType.properties = [new Jsonix.Model.AttributePropertyInfo({
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
  typeInfo: FileMappings.Head
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'body',
  typeInfo: FileMappings.Body
})];

FileMappings.Head.properties = [new Jsonix.Model.ElementPropertyInfo({
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
  typeInfo: FileMappings.Head.Metadata
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'created',
  typeInfo: FileMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'modified',
  typeInfo: FileMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'published',
  typeInfo: FileMappings.Published
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'security',
	typeInfo: FileMappings.Security
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'locked',
	typeInfo: FileMappings.Locked
})];

FileMappings.Head.Metadata.properties = [new Jsonix.Model.ElementMapPropertyInfo({
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
FileMappings.Body.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'contents',
	collection: true,
	elementName: new Jsonix.XML.QName('content'),
	typeInfo: FileMappings.Body.Content
})];

// <composer id="composer-id">...</composer>
FileMappings.Body.Content.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'language',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'created',
  typeInfo: FileMappings.CreatedModified
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
})];

FileMappings.typeInfos = [FileMappings.Head];
FileMappings.elementInfos = [{
  elementName: new Jsonix.XML.QName('files'),
  typeInfo: FileMappings.FilesCollection
},
{
  elementName: new Jsonix.XML.QName('file'),
  typeInfo: FileMappings.FileType
},
{
  elementName: new Jsonix.XML.QName('image'),
  typeInfo: FileMappings.FileType
},
{
  elementName: new Jsonix.XML.QName('head'),
  typeInfo: FileMappings.Head
},
{
  elementName: new Jsonix.XML.QName('content'),
  typeInfo: FileMappings.Body.Content
}];