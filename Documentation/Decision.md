Why did we decide X over Y, at this point in time
Database Design
User History

Here, we are keeping the User and Membership in different tables.

We could keep both in a single table and use a column such as role to identify the user's role. However, this approach does not give us a clean and maintainable history of the user's membership in different rooms.

If we keep the membership history in the same table, we would either need to create another table later for storing the history or keep the history as a list inside a column. The list-based approach can keep increasing day by day, which makes the data harder to query, maintain, and manage.

For this reason, we are skipping the idea of keeping the User and Membership information in the same table.

Room History

There is another problem with keeping the membership history inside the User table.

If we want to check who stayed in a particular room, we would have to check the history_room list/column of every user. This would make querying the room history more complicated and potentially time-consuming.

For that reason, we use two separate tables for User and Membership.

Rooms and Listings

Here also, we could keep both Room and Listing in a single table.

This would allow us to have one service for both the room and listing-related operations. However, the same problem mentioned above would occur.

If we want to know the last posted listing or the listing history of a room, it would become difficult to manage. We would have to create additional columns or store listing history inside the room record.

This also makes it difficult to clearly identify which member posted the current listing for a room, for example, whether it is a replacement listing posted by a leaving member or a listing created for a new member.

For that reason, we use two separate tables for Room and Listing.

However, we still have one problem: we can have multiple listings for the same room over time, but only one listing should be active at a particular time.

For this reason, we keep a status for the listing, such as:

ACTIVE
CLOSED

This allows us to maintain the listing history while still identifying the currently active listing.

For keeping the list of people who are interested in a particular listing, we create a separate Interested table.

This allows us to keep track of which users are interested in which listing without putting that information directly inside the Listing table.

Package Structure
Layer-based / Feature-based

For now, we are choosing a layer-based package structure.

The reason is that a feature-based structure is not necessary at this point because we still have a relatively small number of entities and the application is not yet large enough to require that level of separation.

We currently have only a small number of core entities, so keeping the project simple with a layer-based structure makes more sense.

Also, we are not building this as microservices at this stage, so there is no need to introduce additional complexity in the package structure.

As the number of entities and features grows, we can reconsider moving toward a feature-based structure.