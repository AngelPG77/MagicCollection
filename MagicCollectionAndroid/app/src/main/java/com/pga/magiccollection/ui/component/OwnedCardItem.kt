package com.pga.magiccollection.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.R
import com.pga.magiccollection.domain.model.enums.CardCondition

/**
 * Unified list item for owned cards (collections) and wanted cards (want lists).
 *
 * Layout:
 *  - Left: card art thumbnail
 *  - Right (fills image height via IntrinsicSize):
 *      top — name + edit icon button
 *           type line, optional collection label, optional mana cost
 *      bottom — attribute chips (foil / language / condition) + quantity badge
 */
@Composable
fun OwnedCardItem(
    name: String,
    typeLine: String? = null,
    imageUrl: String? = null,
    quantity: Int,
    foil: Boolean,
    language: String,
    condition: String,
    manaCost: String? = null,
    collectionName: String? = null,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val conditionLabel = CardCondition.entries.find { it.name == condition }?.displayName
        ?: condition.replace("_", " ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            // ── Card art ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(0.718f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(160, 223)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build(),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Right column — stretches to match the image height ───────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_edit),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (!typeLine.isNullOrBlank()) {
                        Text(
                            text = typeLine,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (!collectionName.isNullOrEmpty()) {
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!manaCost.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        ManaCostRow(manaCost = manaCost, pipSize = 16.dp)
                    }
                }

                // Bottom section: chips left, quantity badge right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (foil) {
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = stringResource(R.string.wantlist_card_foil),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = language.uppercase(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = conditionLabel,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }

                    // Quantity badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "×$quantity",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
