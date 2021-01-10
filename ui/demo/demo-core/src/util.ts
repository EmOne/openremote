import {Asset, AssetQuery, AssetQueryMatch, WellknownAssets} from "@openremote/model";
import manager from "@openremote/core";

export async function getBuildingAsset(): Promise<Asset | undefined> {
    const query: AssetQuery = {
        names: [{
            predicateType: "string",
            match: AssetQueryMatch.EXACT,
            value: "De Rotterdam"
        }],
        types: [WellknownAssets.BUILDINGASSET],
        select: {
            excludePath: true,
            excludeAttributes: true,
            excludeParentInfo: true
        }
    };

    const response = await manager.rest.api.AssetResource.queryAssets(query);
    const assets = response.data;

    if (assets.length !== 1) {
        console.log("Failed to retrieve the asset");
        return;
    }
    return assets[0];
}
