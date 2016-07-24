UPDATE `donation_votes`.`location`
SET
`District_Number` = <{District_Number: }>,
`District_Map` = <{District_Map: }>,
`District_Wikipedia_Map` = <{District_Wikipedia_Map: }>,
`District_Wikipedia_Page` = <{District_Wikipedia_Page: }>
WHERE `District_Number` = <{expr}>;
