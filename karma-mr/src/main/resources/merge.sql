CREATE TEMPORARY FUNCTION MergeJSON as 'edu.isi.karma.mapreduce.function.MergeJSON';
CREATE TEMPORARY FUNCTION SplitAndCleanJSONArray as 'edu.isi.karma.mapreduce.function.SplitAndCleanJSONArray';
INSERT INTO TABLE ${TABLE_NAME}
select MergeJSON(d.json, source_table.json, ${JSON_PATH_TO_MERGE}) as result, d.uri_to_merge, source_table.json from ${SOURCE_TABLE_NAME} source_table right outer join 
	( select b.json as json, trim(c.uri_to_merge) as uri_to_merge from
		(select a.json as json, SplitAndCleanJSONArray(if(a.uris_to_merge_string IS NULL, "", a.uris_to_merge_string)) as uris_to_merge_array from 
			(select json, get_json_object(json, ${JSON_PATH_FOR_MERGE_URIS}) as uris_to_merge_string from 
				${TARGET_TABLE_NAME} 
			) a
		) b  lateral view explode(uris_to_merge_array) c as uri_to_merge
	) d 
on d.uri_to_merge== get_json_object(source_table.json, "$._id")